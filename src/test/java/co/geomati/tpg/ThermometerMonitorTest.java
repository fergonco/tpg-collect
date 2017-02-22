package co.geomati.tpg;

import static org.mockito.Mockito.doThrow;
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

public class ThermometerMonitorTest {

	private DayFrame dayFrame;
	private ThermometerComparator comparator;
	private ThermometerMonitor monitor;
	private WeatherArchiverImpl weatherArchiver;
	private HumanReadableLog hrLog;

	@Before
	public void setup() {
		dayFrame = mock(DayFrame.class);
		comparator = mock(ThermometerComparator.class);
		weatherArchiver = mock(WeatherArchiverImpl.class);
		hrLog = mock(HumanReadableLogImpl.class);
		monitor = new ThermometerMonitor(dayFrame, new ThermometerComparator[] { comparator }, weatherArchiver, hrLog);
	}

	@Test
	public void testDayNotStarted() throws IOException, SAXException, ParseException {

		when(dayFrame.getCurrentDay()).thenReturn(null);
		when(dayFrame.getWaitingMSUntilTomorrow()).thenReturn(10000L);

		monitor.monitor();

		verify(comparator, never()).init();
		verify(comparator, never()).check();
	}

	@Test
	public void testWeatherReport() throws IOException, SAXException, ParseException {

		when(dayFrame.getCurrentDay()).thenReturn(null);
		when(dayFrame.getWaitingMSUntilTomorrow()).thenReturn(10000L);

		monitor.monitor();

		verify(weatherArchiver).archive();
	}

	@Test
	public void testDayStarted() throws IOException, SAXException, ParseException {
		when(dayFrame.getCurrentDay()).thenReturn(new Date(0));

		monitor.monitor();

		verify(comparator, times(1)).init();
		verify(comparator, times(1)).check();
	}

	@Test
	public void retryInitIfFailure() throws IOException, SAXException, ParseException {
		when(dayFrame.getCurrentDay()).thenReturn(new Date(0));
		doThrow(new NullPointerException()).doNothing().when(comparator).init();

		monitor.monitor();
		monitor.monitor();

		verify(comparator, times(2)).init();
		verify(comparator, times(1)).check();
	}

	@Test
	public void testDayFinished() throws IOException, SAXException, ParseException {
		when(dayFrame.getCurrentDay()).thenReturn(new Date(0)).thenReturn(null);
		when(dayFrame.getWaitingMSUntilTomorrow()).thenReturn(10000L);

		monitor.monitor();
		verify(comparator, times(1)).init();
		verify(comparator, times(1)).check();
		verify(comparator, never()).clear();

		monitor.monitor();
		verify(comparator, times(1)).clear();
	}

	@Test
	public void testFailInComparatorRaisesNoException() throws IOException, SAXException, ParseException {
		when(dayFrame.getCurrentDay()).thenReturn(new Date(0));

		doThrow(new RuntimeException()).when(comparator).check();

		monitor.monitor();
	}
}
