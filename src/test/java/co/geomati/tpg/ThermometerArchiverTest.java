package co.geomati.tpg;

import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.xml.sax.SAXException;

public class ThermometerArchiverTest {

	@Test
	public void testCheckCloseDeparture() throws IOException, SAXException,
			ParseException {
		File folder = new File("target/temp");
		FileUtils.deleteDirectory(folder);

		ArrayList<Step> steps = new ArrayList<Step>();
		Step step = new Step();
		step.setDepartureCode("12345");
		step.setStopCode("STOP");
		step.setTimestamp(1000);
		step.setActualTimestamp(2000);
		steps.add(step);
		Thermometer thermometer = new Thermometer(steps);

		ThermometerArchiver archiver = new ThermometerArchiver(folder);
		archiver.archive("Y", "FEMA", "VAL-THOIRY", thermometer);

		String thermometerContent = IOUtils.toString(
				folder.listFiles()[0].toURI(), Charset.forName("utf-8"));
		assertTrue(thermometerContent.equals("STOP|12345|1000|2000\n"));
	}
}
