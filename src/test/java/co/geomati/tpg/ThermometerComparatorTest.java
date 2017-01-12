package co.geomati.tpg;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import co.geomati.tpg.utils.TPGCachedParser;

public class ThermometerComparatorTest {

	private DayFrame dayFrame;
	private TPGCachedParser tpg;
	private ThermometerArchiverImpl archiver;
	private ThermometerComparator comparator;
	private ThermometerStart start;
	private String departureCode;
	private Thermometer referenceThermometer;
	private Thermometer updateThermometer;

	@Before
	public void setup() throws IOException, SAXException, ParseException {
		dayFrame = mock(DayFrame.class);
		tpg = mock(TPGCachedParser.class);
		archiver = mock(ThermometerArchiverImpl.class);
		comparator = new ThermometerComparator(dayFrame, tpg, archiver, "18",
				"CERN", "BLANCHE");

		start = new ThermometerStart();
		departureCode = "12345";
		start.setDepartureCode(departureCode);

		ThermometerStart[] starts = new ThermometerStart[] { start };
		when(
				tpg.getStopDepartures(any(Date.class), anyString(),
						anyString(), anyString())).thenReturn(starts);

		referenceThermometer = mock(Thermometer.class);
		updateThermometer = mock(Thermometer.class);
		when(tpg.getThermometer(departureCode))
				.thenReturn(referenceThermometer).thenReturn(updateThermometer);
	}

	@Test
	public void testCheckGetThermometersWithMetadata() throws IOException,
			SAXException, ParseException {
		long tooEarlyToBeChecked = new Date().getTime() + 3 * 60 * 60 * 1000;
		start.setTimestamp(tooEarlyToBeChecked);

		assertEquals(0, comparator.getThermometers().size());
		comparator.init();
		assertEquals(1, comparator.getThermometers().size());
	}

	@Test
	public void testCheckTooEarly() throws IOException, SAXException,
			ParseException {
		long tooEarlyToBeChecked = new Date().getTime() + 3 * 60 * 60 * 1000;
		start.setTimestamp(tooEarlyToBeChecked);

		comparator.init();
		comparator.check();

		verify(tpg, times(1)).getThermometer(departureCode);
	}

	@Test
	public void testCheckDoneAreNotProcessed() throws IOException,
			SAXException, ParseException {
		long inThePast = new Date().getTime();
		start.setTimestamp(inThePast);

		when(referenceThermometer.isDone()).thenReturn(true);

		comparator.init();
		verify(tpg, times(1)).getThermometer(departureCode);
		comparator.check();
		verify(tpg, times(1)).getThermometer(departureCode);

	}

	@Test
	public void testCheckCloseDepartureThermometerUpdate() throws IOException,
			SAXException, ParseException {
		long closeEnoughToBeProcessed = new Date().getTime() + 60000;
		start.setTimestamp(closeEnoughToBeProcessed);

		comparator.init();
		verify(tpg, times(1)).getThermometer(departureCode);
		comparator.check();
		verify(tpg, times(2)).getThermometer(departureCode);
		verify(referenceThermometer).update(updateThermometer);
	}

	@Test
	public void testCheckCloseDepartureThermometerNotComplete()
			throws IOException, SAXException, ParseException {
		long closeEnoughToBeProcessed = new Date().getTime() + 60000;
		start.setTimestamp(closeEnoughToBeProcessed);

		comparator.init();
		comparator.check();
		verify(referenceThermometer).update(updateThermometer);
		verify(referenceThermometer, never()).setDone(anyBoolean());
		verify(archiver, never()).archive(anyString(), anyString(),
				anyString(), eq(referenceThermometer));
	}

	@Test
	public void testCheckCloseDepartureThermometerCompleteWithEffectiveUpdate()
			throws IOException, SAXException, ParseException {
		long closeEnoughToBeProcessed = new Date().getTime() + 60000;
		start.setTimestamp(closeEnoughToBeProcessed);

		when(referenceThermometer.isComplete()).thenReturn(true);
		when(referenceThermometer.update(any(Thermometer.class))).thenReturn(
				true);

		comparator.init();
		comparator.check();
		verify(referenceThermometer).update(updateThermometer);
		verify(referenceThermometer, never()).setDone(anyBoolean());
		verify(archiver).archive(anyString(), anyString(), anyString(),
				eq(referenceThermometer));
	}

	@Test
	public void testCheckCloseDepartureThermometerCompleteWithNoEffectiveUpdate()
			throws IOException, SAXException, ParseException {
		long closeEnoughToBeProcessed = new Date().getTime() + 60000;
		start.setTimestamp(closeEnoughToBeProcessed);

		when(referenceThermometer.isComplete()).thenReturn(true);
		when(referenceThermometer.update(any(Thermometer.class))).thenReturn(
				false);

		comparator.init();
		comparator.check();
		verify(referenceThermometer).update(updateThermometer);
		verify(referenceThermometer).setDone(anyBoolean());
		verify(archiver).archive(anyString(), anyString(), anyString(),
				eq(referenceThermometer));
	}

	@Test
	public void initOkAfterClear() throws IOException, SAXException,
			ParseException {
		comparator.clear();
		comparator.init();
	}

}
