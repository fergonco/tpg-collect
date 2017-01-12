package co.geomati.tpg;

import java.io.IOException;

public interface ThermometerArchiver {

	void archive(String line, String firstStop, String destination, Thermometer thermometer) throws IOException;

}