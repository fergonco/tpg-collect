package co.geomati.tpg;

public interface ThermometerListener {

	void stepActualTimestampChanged(Step previousStep, Step currentStep, String direction);

}
