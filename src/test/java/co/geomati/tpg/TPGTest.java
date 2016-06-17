package co.geomati.tpg;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;

import org.junit.Test;
import org.mockito.Matchers;
import org.xml.sax.SAXException;

import co.geomati.tpg.utils.TPG;
import co.geomati.tpg.utils.TPGCachedParser;

public class TPGTest extends AbstractTest {

	@Test
	public void testGetThermometer() throws IOException, SAXException,
			ParseException {
		TPG tpg = mock(TPG.class);
		String resourceContent = getResource("GetThermometer.xml");
		when(tpg.get(eq("GetThermometer.xml"), Matchers.<String> anyVararg()))
				.thenReturn(resourceContent);

		TPGCachedParser tpgParser = new TPGCachedParser(tpg);
		Thermometer thermometer = tpgParser.getThermometer("12345");
		Collection<Step> steps = thermometer.getSteps();
		assertEquals(22, steps.size());
		Step first = steps.iterator().next();
		assertEquals("BLAN", first.getStopCode());
		assertEquals("50460", first.getDepartureCode());
		assertEquals(1466563320000L, first.getTimestamp());
	}

	@Test
	public void testGetStopDepartures() throws IOException, SAXException,
			ParseException {
		TPG tpg = mock(TPG.class);
		String resourceContent = getResource("GetAllNextDepartures.xml");
		when(
				tpg.get(eq("GetAllNextDepartures.xml"),
						Matchers.<String> anyVararg())).thenReturn(
				resourceContent);

		TPGCachedParser tpgParser = new TPGCachedParser(tpg);
		ThermometerStart[] starts = tpgParser.getStopDepartures(new Date(),
				"18", "CERN", "BLANCHE");
		assertEquals(70, starts.length);
		assertEquals("51725", starts[0].getDepartureCode());
		assertEquals(1466062350000L, starts[0].getTimestamp());
	}
}
