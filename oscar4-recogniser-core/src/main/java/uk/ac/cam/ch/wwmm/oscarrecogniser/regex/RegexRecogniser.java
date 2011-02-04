package uk.ac.cam.ch.wwmm.oscarrecogniser.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.cam.ch.wwmm.oscar.document.IToken;
import uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.interfaces.ChemicalEntityRecogniser;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;


/**
 * 
 * A class to recognise named entities that match a given regular expression.
 * Intended to be used to recognise serial numbers etc.
 * 
 * @author dmj30
 *
 */
public class RegexRecogniser implements ChemicalEntityRecogniser {

	private Pattern pattern;
	private NamedEntityType neType = NamedEntityType.COMPOUND;
	
	public RegexRecogniser(String regex) {
		if ("".equals(regex)) {
			throw new IllegalArgumentException("regex must not be empty");
		}
		pattern = Pattern.compile(regex);
	}

	
	public List<NamedEntity> findNamedEntities(
			List<ITokenSequence> tokenSequences) {
		
		List <NamedEntity> nes = new ArrayList<NamedEntity>();
		for (ITokenSequence tokSeq : tokenSequences) {
			Matcher matcher = pattern.matcher(tokSeq.getSurface());
			while(matcher.find()) {
				IToken startToken = tokSeq.getTokenByStartIndex(matcher.start());
				IToken endToken = tokSeq.getTokenByEndIndex(matcher.end());
				if (startToken != null && endToken != null) {
					List <IToken> tokens = new ArrayList<IToken>(tokSeq.getTokens());
					IToken token;
					while ((token = tokens.get(0)) != startToken) {
						tokens.remove(token);
					}
					while ((token = tokens.get(tokens.size()-1)) != endToken) {
						tokens.remove(token);
					}
					NamedEntity ne = new NamedEntity(tokens, matcher.group(), neType);
					nes.add(ne);
				}
			}
		}
		
		return nes;
	}

	
	/**
	 * @return the pattern matched by the RegexRecogniser
	 */
	public Pattern getPattern() {
		return pattern;
	}


	/**
	 * Sets the NamedEntityType to be applied to named entities identified
	 * by the RegexRecogniser.
	 * @param neType
	 */
	public void setNamedEntityType(NamedEntityType neType) {
		this.neType  = neType;
	}

}