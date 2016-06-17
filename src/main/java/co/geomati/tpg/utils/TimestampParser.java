package co.geomati.tpg.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class TimestampParser {
	private static final String pattern = "yyyy-MM-dd'T'HH:mm:ssZ";
	private static final SimpleDateFormat format = new SimpleDateFormat(pattern);

	public long getTime(String textContent) throws ParseException {
		return format.parse(textContent).getTime();
	}

}
