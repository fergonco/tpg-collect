package co.geomati.tpg;

import java.util.Date;

public class Step {

	private String departureCode;
	private long timestamp;
	private long actualTimestamp = -1;
	private String stopCode;
	private boolean reliable;

	public String getDepartureCode() {
		return departureCode;
	}

	public void setDepartureCode(String departureCode) {
		this.departureCode = departureCode;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getStopCode() {
		return stopCode;
	}

	public void setStopCode(String stopCode) {
		this.stopCode = stopCode;
	}

	public boolean isReliable() {
		return reliable;
	}

	public void setReliable(boolean reliable) {
		this.reliable = reliable;
	}

	public long getActualTimestamp() {
		return actualTimestamp;
	}

	public void setActualTimestamp(long actualTimestamp) {
		this.actualTimestamp = actualTimestamp;
	}

	@Override
	public String toString() {
		String ret = stopCode + "(" + departureCode + "): "
				+ new Date(timestamp);
		if (actualTimestamp != -1) {
			ret += "(" + new Date(actualTimestamp) + ", "
					+ (actualTimestamp - timestamp) + " difference)";
		}
		return ret;
	}

}
