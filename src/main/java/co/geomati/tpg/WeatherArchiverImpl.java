package co.geomati.tpg;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WeatherArchiverImpl implements WeatherArchiver {

	private final static Logger logger = LogManager
			.getLogger(WeatherArchiverImpl.class);

	private File resultsFolder;

	public WeatherArchiverImpl(File resultsFolder) {
		this.resultsFolder = resultsFolder;
	}

	/* (non-Javadoc)
	 * @see co.geomati.tpg.WeatherArchiver#archive()
	 */
	public void archive() {
		Date today = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy_HH_mm");
		File file = new File(resultsFolder, "weather-" + sdf.format(today)
				+ ".log");
		String weatherReport = null;
		try {
			weatherReport = getWeatherReport();
		} catch (IOException e) {
			logger.error("error getting weather report", e);
		}
		if (weatherReport != null) {
			try {
				BufferedOutputStream stream = new BufferedOutputStream(
						new FileOutputStream(file));
				IOUtils.write(weatherReport.getBytes(), stream);
				stream.close();
			} catch (IOException e) {
				logger.error("Error writing weather report", e);
			}
		}
	}

	private String getWeatherReport() throws IOException {
		String url = "http://api.openweathermap.org/data/2.5/weather?id=2660646&appid=d851e08972b5a3cff914ea414c720bdf";
		try {
			return IOUtils.toString(new URI(url), Charset.forName("utf-8"));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

}
