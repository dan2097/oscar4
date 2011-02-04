package uk.ac.cam.ch.wwmm.oscar.chemnamedict.data;

import java.net.URI;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.ImmutableChemNameDict;

public class ImmutableChemNameDictTest {

	@Test
	public void testConstructor() throws Exception {
		Assert.assertNotNull(
			new ImmutableChemNameDict(
				new URI("http://example.com/"),
				Locale.ENGLISH
			)
		);
	}

}