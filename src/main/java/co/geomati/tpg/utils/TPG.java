package co.geomati.tpg.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TPG {

	private final static Logger logger = LogManager.getLogger(TPG.class);

	private static String key;
	private static final String baseURL = "http://prod.ivtr-od.tpg.ch/v1/";

	static {
		InputStream stream = TPG.class.getResourceAsStream("/tpgkey");
		try {
			key = IOUtils.toString(stream, Charset.forName("utf8"));
		} catch (IOException e) {
			throw new RuntimeException("Key not found", e);
		}
	}

	public String get(String command, String... params) throws IOException {
		String url = baseURL + command + "?key=" + key;
		for (int i = 0; i < params.length; i++) {
			url += "&" + params[i];
		}
		logger.debug(url);
		String ret;
		BufferedInputStream bis = new BufferedInputStream(new URL(url).openStream());
		ret = IOUtils.toString(bis, Charset.forName("utf-8"));
		bis.close();
		logger.debug("ok");
		return ret;
	}

}
