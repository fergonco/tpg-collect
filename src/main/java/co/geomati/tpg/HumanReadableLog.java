package co.geomati.tpg;

import java.io.IOException;
import java.util.List;

public interface HumanReadableLog {

	void log(List<Thermometer> thermometers) throws IOException;

}