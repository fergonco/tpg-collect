package co.geomati.tpg.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
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
		try {
			String url = baseURL + command + "?key=" + key;
			for (int i = 0; i < params.length; i++) {
				url += "&" + params[i];
			}
			logger.debug(url);
			String ret = IOUtils.toString(new URI(url),
					Charset.forName("utf-8"));
			logger.debug("ok");
			logger.debug(ret);
			return ret;
		} catch (URISyntaxException e) {
			throw new RuntimeException("Bug: Malformed URI", e);
		}
	}

}
