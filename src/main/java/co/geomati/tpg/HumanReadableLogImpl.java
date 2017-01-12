package co.geomati.tpg;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class HumanReadableLogImpl implements HumanReadableLog {

	private File resultsFolder;

	public HumanReadableLogImpl(File resultsFolder) {
		this.resultsFolder = resultsFolder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see co.geomati.tpg.HumanReadableLog#log(java.util.List)
	 */
	public void log(List<Thermometer> thermometers) throws IOException {
		StringBuilder builder = format(thermometers);

		File thermometersFile = getLogFile();
		BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(thermometersFile));
		IOUtils.write(builder.toString().getBytes(), stream);
		stream.close();
	}

	public static StringBuilder format(List<Thermometer> thermometers) {
		StringBuilder builder = new StringBuilder();
		builder.append("\n\n\nLast update " + new Date()).append("\n");
		for (Thermometer thermometer : thermometers) {
			ThermometerMetadata metadata = thermometer.getMetadata();
			builder.append("****************************************************** ").append(metadata.getLine())
					.append(" ").append(metadata.getFirstStop()).append(" ").append(metadata.getDestination())
					.append(" ********").append("\n");
			Collection<Step> steps = thermometer.getSteps();
			for (Step step : steps) {
				builder.append(step).append("\n");
			}
			builder.append("************************************************").append("\n");
		}
		return builder;
	}

	public File getLogFile() {
		return new File(resultsFolder, "thermometers.log");
	}
}
