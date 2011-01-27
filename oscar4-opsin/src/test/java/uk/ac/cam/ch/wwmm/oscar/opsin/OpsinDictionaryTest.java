package uk.ac.cam.ch.wwmm.oscar.opsin;

import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.XPathContext;

import org.junit.Assert;
import org.junit.Test;

public class OpsinDictionaryTest {

	@Test
	public void testMethaneInChI() {
		OpsinDictionary dict = new OpsinDictionary();
		Assert.assertNotNull(dict);
		Assert.assertEquals(
			"InChI=1/CH4/h1H4",
			dict.getInChI("methane").iterator().next()
		);
	}
	
	@Test
	public void testMethaneCML() {
		OpsinDictionary dict = new OpsinDictionary();
		Assert.assertNotNull(dict);
		Element cml = dict.getCML("methane").iterator().next();
		Nodes atoms = cml.query("//cml:atom", new XPathContext("cml", "http://www.xml-cml.org/schema"));
		Assert.assertEquals(5, atoms.size());
		Nodes bonds = cml.query("//cml:bond", new XPathContext("cml", "http://www.xml-cml.org/schema"));
		Assert.assertEquals(4, bonds.size());
	}
	
	@Test
	public void testMethaneSmiles() {
		OpsinDictionary dict = new OpsinDictionary();
		Assert.assertNotNull(dict);
		Assert.assertEquals("C", dict.getSMILES("methane").iterator().next());
	}

	@Test
	public void testBenzeneInChI() {
		OpsinDictionary dict = new OpsinDictionary();
		Assert.assertNotNull(dict);
		Assert.assertEquals(
			"InChI=1/C6H6/c1-2-4-6-5-3-1/h1-6H",
			dict.getInChI("benzene").iterator().next()
		);
	}
	
	@Test
	public void testBenzeneCML(){
		OpsinDictionary dict = new OpsinDictionary();
		Assert.assertNotNull(dict);
		Element cml = dict.getCML("benzene").iterator().next();
		Nodes atoms = cml.query("//cml:atom", new XPathContext("cml", "http://www.xml-cml.org/schema"));
		Assert.assertEquals(12, atoms.size());
		Nodes bonds = cml.query("//cml:bond", new XPathContext("cml", "http://www.xml-cml.org/schema"));
		Assert.assertEquals(12, bonds.size());
	}

	@Test
	public void testBenzeneSmiles() {
		OpsinDictionary dict = new OpsinDictionary();
		Assert.assertNotNull(dict);
		Assert.assertEquals("C1=CC=CC=C1", dict.getSMILES("benzene").iterator().next());
	}
}
