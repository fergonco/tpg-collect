package co.geomati.tpg;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class AbstractTest {

	protected String getResource(String resourceName) throws IOException {
		InputStream stream = this.getClass().getResourceAsStream(resourceName);
		String ret = IOUtils.toString(stream, "utf-8");
		stream.close();

		return ret;
	}
}
