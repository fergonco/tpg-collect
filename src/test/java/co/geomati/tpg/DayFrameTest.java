package co.geomati.tpg;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import co.geomati.tpg.DayFrame.DateProvider;

public class DayFrameTest {

	@Test
	public void dayNotStarted() throws Exception {
		DateProvider dateProvider = mock(DayFrame.DateProvider.class);
		when(dateProvider.getNow()).thenReturn(3 * 60 * 60 * 1000L);
		DayFrame dayFrame = new DayFrame(4 * 60 * 60 * 1000,
				27 * 60 * 60 * 1000, dateProvider);
		assertNull(dayFrame.getCurrentDay());
	}

	@Test
	public void dayStarted() throws Exception {
		DateProvider dateProvider = mock(DayFrame.DateProvider.class);
		when(dateProvider.getNow()).thenReturn(5 * 60 * 60 * 1000L);
		DayFrame dayFrame = new DayFrame(4 * 60 * 60 * 1000,
				27 * 60 * 60 * 1000, dateProvider);
		assertEquals(0, dayFrame.getCurrentDay().getTime());
	}

	@Test
	public void dayFinished() throws Exception {
		DateProvider dateProvider = mock(DayFrame.DateProvider.class);
		when(dateProvider.getNow()).thenReturn(27 * 60 * 60 * 1000L + 1);
		DayFrame dayFrame = new DayFrame(4 * 60 * 60 * 1000,
				27 * 60 * 60 * 1000, dateProvider);
		assertNull(dayFrame.getCurrentDay());
	}

	@Test
	public void nextDay() throws Exception {
		DateProvider dateProvider = mock(DayFrame.DateProvider.class);
		when(dateProvider.getNow()).thenReturn(29 * 60 * 60 * 1000L + 1);
		DayFrame dayFrame = new DayFrame(4 * 60 * 60 * 1000,
				27 * 60 * 60 * 1000, dateProvider);
		assertEquals(24 * 60 * 60 * 1000, dayFrame.getCurrentDay().getTime());
	}

	@Test
	public void millisUntilNextDay() throws Exception {
		DateProvider dateProvider = mock(DayFrame.DateProvider.class);
		when(dateProvider.getNow()).thenReturn(27 * 60 * 60 * 1000L + 1);
		DayFrame dayFrame = new DayFrame(4 * 60 * 60 * 1000,
				27 * 60 * 60 * 1000, dateProvider);

		System.out.println(dayFrame.getWaitingMSUntilTomorrow());
		assertEquals(3599999, dayFrame.getWaitingMSUntilTomorrow());
	}
}
