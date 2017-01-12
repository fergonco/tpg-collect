package co.geomati.tpg;

import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class HumanReadableLogTest {

	@Test
	public void testWrite() throws IOException {
		HumanReadableLogImpl log = new HumanReadableLogImpl(new File("target/temp"));
		log.getLogFile().delete();
		List<Thermometer> thermometers = new ArrayList<Thermometer>();
		Thermometer thermometer = new Thermometer(getSteps());
		thermometer.setMetadata("LINE", "ORIGIN", "ANYWHERE");
		thermometers.add(thermometer);
		log.log(thermometers);

		assertTrue(log.getLogFile().exists());
	}

	private ArrayList<Step> getSteps() {
		ArrayList<Step> ret = new ArrayList<Step>();
		Step step = new Step();
		step.setStopCode("ORIGIN");
		step.setDepartureCode("12345");
		step.setTimestamp(0L);
		ret.add(step);
		step = new Step();
		step.setStopCode("TARGET");
		step.setDepartureCode("54321");
		step.setTimestamp(21000L);
		ret.add(step);
		return ret;
	}
}
