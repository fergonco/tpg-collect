package co.geomati.tpg;

import static junit.framework.Assert.assertEquals;

import java.text.ParseException;

import org.junit.Test;

import co.geomati.tpg.utils.TimestampParser;

public class TimestampParserTest {

	@Test
	public void timezone() throws ParseException {
		TimestampParser parser = new TimestampParser();
		long timezone1 = parser.getTime("2017-04-04T00:55:00+0200");
		long timezone2 = parser.getTime("2017-04-04T00:55:00+0400");

		assertEquals(1000 * 60 * 60 * 2, timezone1 - timezone2);
	}
}
