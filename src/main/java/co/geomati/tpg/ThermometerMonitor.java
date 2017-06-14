package co.geomati.tpg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ThermometerMonitor {
	private final static Logger logger = LogManager.getLogger(ThermometerMonitor.class);

	/*
	 * Thermometers are considered done when they are complete and they don't
	 * get updated. If the pause is too small it can happen that a thermometer
	 * hasn't changed although it will. Therefore this value should be big
	 * enough to a) let thermometers stabilize when complete and b) don't make
	 * more than 5000 calls a day
	 */
	private static final int CHECK_PAUSE = 10 * 60 * 1000;
	private DayFrame dayFrame;
	private ThermometerComparator[] thermometerComparators;
	private WeatherArchiver weatherArchiver;
	private HumanReadableLog hrLog;
	private boolean working = false;
	private Timer waitingTimer;

	public ThermometerMonitor(DayFrame dayFrame, ThermometerComparator[] comparators, WeatherArchiver weatherArchiver,
			HumanReadableLog hrLog) {
		this.dayFrame = dayFrame;
		this.thermometerComparators = comparators;
		this.weatherArchiver = weatherArchiver;
		this.hrLog = hrLog;
	}

	public void monitor() {
		long wait = CHECK_PAUSE;
		try {
			weatherArchiver.archive();
			logHumamReadable();
			Date day = dayFrame.getCurrentDay();
			if (day == null) {
				if (working) {
					for (ThermometerComparator thermometerComparator : thermometerComparators) {
						thermometerComparator.clear();
					}
				}
				wait = dayFrame.getWaitingMSUntilTomorrow();
				working = false;
			} else {
				try {
					working = true;
					for (ThermometerComparator thermometerComparator : thermometerComparators) {
						try {
							thermometerComparator.init();
							thermometerComparator.check();
						} catch (RuntimeException e) {
							logger.error("Error comparing thermometers", e);
						}
					}
				} catch (Exception e) {
					logger.debug("Problem initializing thermometers", e);
					working = false;
				}
			}
		} catch (RuntimeException e) {
			logger.error("unexpected error", e);
		} finally {
			wakeMe(wait);
		}
	}

	private void logHumamReadable() {
		ArrayList<Thermometer> thermometers = new ArrayList<Thermometer>();
		for (ThermometerComparator thermometerComparator : thermometerComparators) {
			thermometers.addAll(thermometerComparator.getThermometers());
		}
		try {
			hrLog.log(thermometers);
		} catch (IOException e) {
			logger.error("Error populating human readable log", e);
		}
	}

	private void wakeMe(long checkPause) {
		waitingTimer = new Timer(true);
		logger.debug("Waiting " + checkPause + " ms until the next wake up");
		waitingTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				monitor();
			}
		}, checkPause);
	}

	public void stop() {
		if (waitingTimer != null) {
			waitingTimer.cancel();
		}
	}

}
