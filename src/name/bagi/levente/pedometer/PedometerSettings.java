package name.bagi.levente.pedometer;

import android.content.SharedPreferences;

public class PedometerSettings {

	SharedPreferences mSettings;
	
	public PedometerSettings(SharedPreferences settings) {
		mSettings = settings;
	}
	
	public boolean isMetric() {
		return true; // TODO
	}
	
	public float getStepLength() {
		return 40; // TODO
	}
	
	
}
