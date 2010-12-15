/*
 * Originally RParser.java
 *
 * Created on 13 August 2004, 18:50
 */

package uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis;

import nu.xom.*;
import org.apache.log4j.Logger;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscar.xmltools.XOMTools;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A regex-based parser, finds chemical formulae etcetera.
 *
 * @author  caw47, annexed by ptc24
 */
public class TokenClassifier {

    private static final Logger logger = Logger.getLogger(TokenClassifier.class);

    private static final String REGEX_FILENAME = "tokenLevelRegularExpressions.xml";

    private static ResourceGetter rg = new ResourceGetter("/uk/ac/cam/ch/wwmm/oscarrecogniser/tokenanalysis/");

    // Singleton instance
    private static TokenClassifier defaultInstance = null;

    private Map<String,TokenClass> tokenLevelRegexs;

    private Map<String,String> nodeDict;

    private Document doc;

    /**
     * Private constructor for singleton pattern
     */
    private TokenClassifier() { }

    public static void reinitialise() throws Exception {
        defaultInstance = null;
        getInstance();
    }

    /**
     * Get an instance of the singleton.
     */
    public static TokenClassifier getInstance()  {
        try {
            if (defaultInstance == null) {
                defaultInstance = new TokenClassifier();
                defaultInstance.readXML(rg.getXMLDocument(REGEX_FILENAME));
            }
            return defaultInstance;
        } catch (Exception e) {
            throw new Error(e);
        }
    }


    /**
     * Read in XML file, construct DOM and build RPNode tree
     * @param document XOM Document containing regular expressions for parsing
     */
    private void readXML(Document document) throws Exception {
        logger.debug("Initialising tlrs... ");
        doc = document;
        nodeDict = new HashMap<String,String>();

        tokenLevelRegexs = new LinkedHashMap<String, TokenClass>();
        Elements tlrElems = doc.getRootElement().getFirstChildElement("tlrs").getChildElements("tlr");
        for (int i = 0; i < tlrElems.size(); i++) {
            TokenClass tlr = new TokenClass(tlrElems.get(i), this);
            if(!OscarProperties.getData().useFormulaRegex &&
                    ("formulaRegex".equals(tlr.getName()) ||
                            "groupFormulaRegex".equals(tlr.getName()))) continue;
            if(OscarProperties.getData().useWordShapeHeuristic &&
                    ("potentialAcronymRegex".equals(tlr.getName()))) continue;
            if (tokenLevelRegexs.containsKey(tlr.getName())) {
                logger.warn("Duplicate TokenLevelRegex defined: "+tlr.getName());
            } else {
                tokenLevelRegexs.put(tlr.getName(), tlr);
            }
        }

        nodeDict = null;

        System.gc();
        logger.debug("tlrs initialised");
    }

    // Methods to find and parse nodes

    /** Find a node in doc with specified type and id */
    Node findNode(String targetType, String targetId) {
        Nodes nodes = doc.query("//node");
        for(int i=0; i<nodes.size(); i++) {
            String type = ((Element)nodes.get(i)).getAttributeValue("type");
            String id = ((Element)nodes.get(i)).getAttributeValue("id");
            if((type.equals(targetType)) & (id.equals(targetId))) {
                return nodes.get(i);
            }
        }
        return null;
    }

    String getNodeText(Element node) {
        StringBuffer txt = new StringBuffer();
        for (int i = 0; i < node.getChildCount(); i++) {
            Node child = node.getChild(i);
            if(child instanceof Text) {
                txt.append(child.getValue());
            } else if(child instanceof Element) {
                Element childElem = (Element)child;
                if(childElem.getLocalName().equals("insert")) {
                    String tag = childElem.getAttributeValue("idref");
                    txt.append(getDefText(tag));
                }
            }
        }

        return txt.toString();
    }

    String getDefText(String idref) {
        if(nodeDict.containsKey(idref)) {
            return nodeDict.get(idref);
        }
        Element defElem = null;
        Nodes defNodes = doc.query("//def[@id=\"" + idref + "\"]");
        if(defNodes.size() == 1) {
            defElem = (Element)defNodes.get(0);
        }
        StringBuffer retval = new StringBuffer("");
        if(defElem.getAttributeValue("type").equals("const")) {
            retval.append(getNodeText(defElem));
        } else if(defElem.getAttributeValue("type").equals("list")) {
            retval.append("(");
            int i = 0;
            Elements items = defElem.getChildElements("item");
            for (int j = 0; j < items.size(); j++) {
                if(i>0) {
                    retval.append("|");
                }
                retval.append(getNodeText(items.get(j)));
                i++;
            }
            retval.append(")");
        }
        nodeDict.put(idref, retval.toString());
        return retval.toString();
    }

    /**Sees if the token matches one of the tlrs.
     *
     * @param token
     * @return The named entity type found, or an empty set.
     */
    public Set<NamedEntityType> classifyToken(String token) {
        Set<NamedEntityType> results = Collections.emptySet();
        for (TokenClass tokenLevelRegex : tokenLevelRegexs.values()) {
            if (tokenLevelRegex.isMatch(token)) {
                if (results.isEmpty()) {
                    results = Collections.singleton(tokenLevelRegex.getType());
                } else {
                    if (results.size() == 1) {
                        results = new HashSet<NamedEntityType>(results);
                    }
                    results.add(tokenLevelRegex.getType());
                }
            }
        }
        return results;
    }

    // FIXME optimality
    public boolean isTokenLevelRegexMatch(String token, String tlrName) {
        TokenClass tokenClass = tokenLevelRegexs.get(tlrName);
        if (tokenClass == null) {
            return false;
        }
        return tokenClass.isMatch(token);
    }

    public int makeHash() {
        return XOMTools.documentHash(doc);
    }

    /** A regular expression used to classify individual Tokens.
     *
     * @author ptc24
     *
     */

    static class TokenClass {

        private String regex;
        private NamedEntityType type;
        private Pattern pattern;
        private String name;

        public TokenClass(Element elem, TokenClassifier tlrHolder) {
            type = NamedEntityType.valueOf(elem.getAttributeValue("type"));
            String idRef = elem.getAttributeValue("idref");
            regex = tlrHolder.getDefText(idRef);
            name = elem.getAttributeValue("name");
            pattern = Pattern.compile(regex, Pattern.COMMENTS);
        }

        public boolean isMatch(String s) {
            Matcher m = pattern.matcher(s);
            return m.matches();
        }

        public NamedEntityType getType() {
            return type;
        }

        public String getName() {
            return name;
        }

    }
}
