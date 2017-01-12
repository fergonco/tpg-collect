package co.geomati.tpg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Thermometer {

	private final static Logger logger = LogManager.getLogger(Thermometer.class);

	private LinkedHashMap<String, Step> steps = new LinkedHashMap<String, Step>();
	private boolean done;
	private ThermometerMetadata metadata;
	private ThermometerListener listener;

	public Thermometer(ArrayList<Step> steps) {
		for (Step step : steps) {
			this.steps.put(step.getDepartureCode(), step);
		}
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public boolean update(Thermometer updatedThermometer) {
		boolean effectiveUpdate = false;
		long now = new Date().getTime();
		Set<String> updatedDepartureCodes = updatedThermometer.getStepDepartureCodes();
		for (String departureCode : updatedDepartureCodes) {
			Step plannedStep = steps.get(departureCode);
			if (plannedStep != null) {
				Step updatedStep = updatedThermometer.steps.get(departureCode);

				if (updatedStep.getTimestamp() < now) {
					if (plannedStep.getActualTimestamp() != updatedStep.getTimestamp()) {
						plannedStep.setActualTimestamp(updatedStep.getTimestamp());
						listener.stepActualTimestampChanged(plannedStep);
						effectiveUpdate = true;
					}
				}
			} else {
				logger.error("Inconsistency between planned steps and update: " + departureCode);
			}
		}

		return effectiveUpdate;
	}

	public boolean isComplete() {
		for (Step step : steps.values()) {
			if (step.getActualTimestamp() == -1) {
				return false;
			}
		}

		return true;
	}

	private Set<String> getStepDepartureCodes() {
		return steps.keySet();
	}

	public void report() {
		System.out.println("************************************************");
		Set<String> departureCodes = steps.keySet();
		for (String departureCode : departureCodes) {
			Step step = steps.get(departureCode);
			System.out.println(step);
		}
		System.out.println("************************************************");
	}

	public Collection<Step> getSteps() {
		return steps.values();
	}

	public String getReport() {
		StringBuilder builder = new StringBuilder();
		Set<String> departureCodes = steps.keySet();
		for (String departureCode : departureCodes) {
			Step step = steps.get(departureCode);
			builder.append(step.getStopCode()).append("|")//
					.append(step.getDepartureCode()).append("|")//
					.append(step.getTimestamp()).append("|")//
					.append(step.getActualTimestamp()).append("\n");
		}
		return builder.toString();
	}

	public void setMetadata(String line, String firstStop, String destination) {
		this.metadata = new ThermometerMetadata(line, firstStop, destination);
	}

	public ThermometerMetadata getMetadata() {
		return metadata;
	}

	public void setListener(ThermometerListener listener) {
		this.listener = listener;
	}

}
