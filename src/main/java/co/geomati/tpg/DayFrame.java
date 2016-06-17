package co.geomati.tpg;

import java.util.Date;

public class DayFrame {

	private int startOffset;
	private int endOffset;
	private DateProvider dateProvider;

	public DayFrame(int startOffset, int endOffset) {
		this(startOffset, endOffset, new DefaultDateProvider());
	}

	public DayFrame(int startOffset, int endOffset, DateProvider dateProvider) {
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.dateProvider = dateProvider;
	}

	public Date getCurrentDay() {
		long now = dateProvider.getNow();
		int dayMillis = 1000 * 60 * 60 * 24;
		long currentOffset = now % dayMillis;
		if (currentOffset > startOffset && currentOffset < endOffset) {
			return new Date(dayMillis * (now / dayMillis));
		} else {
			return null;
		}
	}

	public long getWaitingMSUntilTomorrow() {
		long now = new Date().getTime();
		long currentOffset = now % (1000 * 60 * 60 * 24);
		return endOffset - currentOffset;
	}

	interface DateProvider {
		long getNow();
	}

	private static class DefaultDateProvider implements DateProvider {

		public long getNow() {
			return new Date().getTime();
		}

	}
}
