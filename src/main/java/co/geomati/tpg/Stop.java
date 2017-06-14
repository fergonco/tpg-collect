package co.geomati.tpg;

public class Stop {

	private String code;
	private double lat;
	private double lon;

	public Stop(String code, double lat, double lon) {
		super();
		this.code = code;
		this.lat = lat;
		this.lon = lon;
	}

	public String getCode() {
		return code;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}
}
