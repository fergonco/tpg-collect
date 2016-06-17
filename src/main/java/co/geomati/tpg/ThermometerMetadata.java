package co.geomati.tpg;

public class ThermometerMetadata {

	private String line;
	private String firstStop;
	private String destination;

	public ThermometerMetadata(String line, String firstStop, String destination) {
		this.line = line;
		this.firstStop = firstStop;
		this.destination = destination;
	}

	public String getDestination() {
		return destination;
	}

	public String getFirstStop() {
		return firstStop;
	}

	public String getLine() {
		return line;
	}

}
