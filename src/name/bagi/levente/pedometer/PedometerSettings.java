package name.bagi.levente.pedometer;

import android.content.SharedPreferences;

public class PedometerSettings {

	SharedPreferences mSettings;
	
	public PedometerSettings(SharedPreferences settings) {
		mSettings = settings;
	}
	
	public boolean isMetric() {
		return mSettings.getString("units", "imperial").equals("metric");
	}
	
	public float getStepLength() {
		try {
			return Float.valueOf(mSettings.getString("step_length", "40").trim());
		}
		catch (NumberFormatException e) {
			// TODO: reset value, & notify user somehow
			return 0f;
		}
	}
	
}
