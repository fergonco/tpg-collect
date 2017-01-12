package co.geomati.tpg;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.io.IOUtils;

public class ThermometerArchiverImpl implements ThermometerArchiver {

	private File root;

	public ThermometerArchiverImpl(File folder) {
		this.root = folder;
		if (!root.exists()) {
			root.mkdir();
		}
	}

	/* (non-Javadoc)
	 * @see co.geomati.tpg.ThermometerArchiver#archive(java.lang.String, java.lang.String, java.lang.String, co.geomati.tpg.Thermometer)
	 */
	public void archive(String line, String firstStop, String destination,
			Thermometer thermometer) throws IOException {
		Collection<Step> steps = thermometer.getSteps();
		Step firstStep = steps.iterator().next();
		Date start = new Date(firstStep.getTimestamp());
		SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy_HH_mm");
		File file = new File(root, "thermometer-" + line + "-" + firstStop
				+ "-" + destination + "-" + sdf.format(start) + ".log");
		BufferedOutputStream stream = new BufferedOutputStream(
				new FileOutputStream(file));
		IOUtils.write(thermometer.getReport().getBytes(), stream);
		stream.close();
	}

}
