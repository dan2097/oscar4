package uk.ac.cam.ch.wwmm.oscarrecogniser.manualAnnotations;

import static org.junit.Assert.*;
import nu.xom.Document;
import nu.xom.Element;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscarrecogniser.manualAnnotations.ManualAnnotations;

public class ManualAnnotationsTest {

	@Test
	public void testLoadDefaultModelData() {
		assertNotNull(ManualAnnotations.getDefaultInstance());
	}
	
	@Test
	public void testReadXML() {
		ManualAnnotations manualAnnotations = new ManualAnnotations();
		assertFalse(manualAnnotations.chemicalWords.contains("ammonia"));
		
		ResourceGetter rg = new ResourceGetter("/uk/ac/cam/ch/wwmm/oscarrecogniser/models/");
		Document modelDoc = rg.getXMLDocument("chempapers.xml");
		Element etdElement = modelDoc.getRootElement().getFirstChildElement("etd");

        manualAnnotations = new ManualAnnotations(etdElement);
		assertTrue(manualAnnotations.chemicalWords.contains("ammonia"));
	}

//  XXX Becoming immutable
//	@Test
//	public void testClear() {
//		ExtractedTrainingData etd = ExtractedTrainingData.getInstance();
//		assertFalse(etd.chemicalWords.size() == 0);
//
//		etd.clear();
//		ExtractedTrainingData etd2 = ExtractedTrainingData.getInstance();
//		assertTrue(etd == etd2);
//		assertTrue(etd.chemicalWords.size() == 0);
//	}
	
	@Test
	public void testReinitialise() {
		ManualAnnotations.reinitialise(ManualAnnotations.loadEtdElement("chempapers"));
		assertTrue(ManualAnnotations.getInstance().nonChemicalWords.contains("elongate"));
		assertFalse(ManualAnnotations.getInstance().nonChemicalWords.contains("leukaemic"));
		
		ManualAnnotations.reinitialise(ManualAnnotations.loadEtdElement("pubmed"));
		assertFalse(ManualAnnotations.getInstance().nonChemicalWords.contains("elongate"));
		assertTrue(ManualAnnotations.getInstance().nonChemicalWords.contains("leukaemic"));
	}
}