package uk.ac.cam.ch.wwmm.oscarrecogniser.finder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import uk.ac.cam.ch.wwmm.oscar.document.IToken;
import uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.exceptions.ResourceInitialisationException;
import uk.ac.cam.ch.wwmm.oscar.obo.OntologyTermIdIndex;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

/** A subclass of DFAFinder, used to find named entities that are not found 
 * by the MEMM. Currently handles CUST as well as ONT and CPR.
 * 
 * @author ptc24
 *
 */
public class DFAONTCPRFinder extends DFAFinder {

	/* @dmj30
	 * 
	 * Logging has been disabled as Logger does not implement serialisable
	 * and prevents the serialisation of DFAONTCPRFinder.
	 */
//	private final Logger logger = Logger.getLogger(DFAONTCPRFinder.class);

	private static final long serialVersionUID = -1417523538712568934L;
	private static DFAONTCPRFinder myInstance;
	private static final String SERIALIZED_DFAFINDER = "dfa_ontcpr.dat.gz";

    private static final String REP_ONTWORD = "$ONTWORD";
    private static final String REP_HYPH = "$HYPH";
    private static final String REP_DOTS = "$DOTS";

    /**
	 * Load a DFAONTCPRFinder from the workspace
	 * @return the deserialized DFAONTCPRFinder
	 * @throws IOException 
	 * @throws FileNotFoundException
	 */
	private static DFAONTCPRFinder readFromWorkspace() throws IOException {
		//long time = System.currentTimeMillis();
        InputStream is = DFAONTCPRFinder.class.getResourceAsStream(SERIALIZED_DFAFINDER);
        if (is == null) {
            throw new FileNotFoundException("File not found: "+SERIALIZED_DFAFINDER);
        }
        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new GZIPInputStream(is)));
		DFAONTCPRFinder finder;
		try {
			finder = (DFAONTCPRFinder)ois.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to restore DFAONTCPRFinder from dfas.dat!");
		}
		ois.close();
		return finder;
	}
	
	/**Writes a DFAONTCPRFinder to the workspace.
	 * @param finder The DFAONTCPRFinder to be serialized
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * 
	 */
	private static void writeToWorkspace(DFAONTCPRFinder finder) throws FileNotFoundException, IOException {
		//long time = System.currentTimeMillis();
		ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(
                        new GZIPOutputStream(
                                new FileOutputStream(SERIALIZED_DFAFINDER))));
		oos.writeObject(finder);
		oos.close();
	}
	
	/**
	 * Instantiates a DFAONTCPRFinder then serializes it
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws ResourceInitialisationException 
	 */
	public static void buildAndSerializeDFAONTCPRFinder() throws FileNotFoundException, IOException, ResourceInitialisationException {
		writeToWorkspace(new DFAONTCPRFinder());
	}
	
	/**Get the DFAONTCPRFinder singleton, initialising if necessary.
	 * 
	 * @return The DFAONTCPRFinder singleton.
	 * @throws ResourceInitialisationException 
	 */
	public static DFAONTCPRFinder getInstance() throws ResourceInitialisationException {
		if (myInstance == null) {
//            try {
//    			myInstance = readFromWorkspace();
//            } catch (IOException e) {
//                throw new RuntimeException("Error loading DFAONTCPRFinder data", e);
//            }
            myInstance = new DFAONTCPRFinder();
		}
		return myInstance;
	}
	
	/**Re-initialise the DFAONTCPRFinder singleton.
	 * @throws ResourceInitialisationException 
	 * 
	 */
	public static void reinitialise() throws ResourceInitialisationException {
		myInstance = null;
		getInstance();
	}
	
	/**Destroy the DFAONTCPRFinder singleton.
	 * 
	 */
	public static void destroyInstance() {
		myInstance = null;
	}
	
	/**Checks to see if a string can be tokenised into multiple tokens; if
	 * so, deletes the DFAONTCPRFinder singleton.
	 * 
	 * @param word The string to test.
	 */
	public static void destroyInstanceIfWordTokenises(String word) {
		if (myInstance == null) return;
		ITokenSequence ts = Tokeniser.getInstance().tokenise(word);
		if (ts.getTokens().size() > 1) myInstance = null;
	}
	
	private DFAONTCPRFinder() throws ResourceInitialisationException {
//		logger.debug("Initialising DFA ONT Finder...");
		super.init();
//		logger.debug("Initialised DFA ONT Finder");
	}
	
	@Override
	protected void loadTerms() throws ResourceInitialisationException {
//		logger.debug("Adding ontology terms to DFA finder...");
		for(String s : OntologyTermIdIndex.getInstance().getAllTerms()){
			addNamedEntity(s, NamedEntityType.ONTOLOGY, false);
		}
        for(String s : TermMaps.getInstance().getCustEnt().keySet()){
			addNamedEntity(s, NamedEntityType.CUSTOM, true);
		}
		addNamedEntity(REP_ONTWORD, NamedEntityType.ONTOLOGY, false);
	}
	
	/**Finds the ONT/CPR/CUST NEs from a token sequence.
	 * 
	 * @param tokenSequence The token sequence
	 * @return The NEs.
	 * @throws ResourceInitialisationException 
	 */
	public List<NamedEntity> findNamedEntities(ITokenSequence tokenSequence) throws ResourceInitialisationException {
		NECollector nec = new NECollector();
		List<RepresentationList> repsList = generateTokenRepresentations(tokenSequence);
		findItems(tokenSequence, repsList, nec);
		return nec.getNes();
	}
	
	private List<RepresentationList> generateTokenRepresentations(ITokenSequence tokenSequence) throws ResourceInitialisationException {
		List<RepresentationList> repsList = new ArrayList<RepresentationList>();
		for(IToken token : tokenSequence.getTokens()) {
			repsList.add(generateTokenRepresentations(token));
		}
		return repsList;
	}
	
	protected RepresentationList generateTokenRepresentations(IToken token) throws ResourceInitialisationException {
		RepresentationList representations = new RepresentationList();
		String tokenValue = token.getValue();
		representations.addRepresentation(tokenValue);
		String normalisedValue = StringTools.normaliseName(tokenValue);
		if (!normalisedValue.equals(tokenValue)) {
            representations.addRepresentation(normalisedValue);
        }
		if (OntologyTermIdIndex.getInstance().containsTerm(normalisedValue)) {
            representations.addRepresentation(REP_ONTWORD);
        }
		if (tokenValue.length() == 1) {
			if (StringTools.isHyphen(tokenValue)) {
				representations.addRepresentation(REP_HYPH);
			} else if (StringTools.isMidElipsis(tokenValue)) {
				representations.addRepresentation(REP_DOTS);
			}
		}
		representations.addRepresentations(getSubReRepsForToken(tokenValue));
		return representations;
	}

	public static void main(String[] args) throws Exception {
		buildAndSerializeDFAONTCPRFinder();
	}
}
