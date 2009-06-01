package name.bagi.levente.pedometer;

import android.content.SharedPreferences;

/**
 * Wrapper for {@link SharedPreferences}, handles preferences-related tasks.
 * @author Levente Bagi
 */
public class PedometerSettings {

	SharedPreferences mSettings;
	
	public static int M_NONE = 1;
	public static int M_PACE = 2;
	public static int M_SPEED = 3;
	
	public PedometerSettings(SharedPreferences settings) {
		mSettings = settings;
	}
	
	public boolean isMetric() {
		return mSettings.getString("units", "imperial").equals("metric");
	}
	
	public float getStepLength() {
		try {
			return Float.valueOf(mSettings.getString("step_length", "20").trim());
		}
		catch (NumberFormatException e) {
			// TODO: reset value, & notify user somehow
			return 0f;
		}
	}
	
	public int getMaintainOption() {
		String p = mSettings.getString("maintain", "none");
		return 
			p.equals("none") ? M_NONE : (
			p.equals("pace") ? M_PACE : (
			p.equals("speed") ? M_SPEED : ( 
			0)));
	}
	
	//-------------------------------------------------------------------
	// Desired pace & speed: 
	// these can not be set in the preference activity, only on the main
	// screen if "maintain" is set to "pace" or "speed" 
	
	public int getDesiredPace() {
		return mSettings.getInt("desired_pace", 180); // steps/minute
	}
	public float getDesiredSpeed() {
		return mSettings.getFloat("desired_speed", 4f); // km/h or mph
	}
	public void savePaceOrSpeedSetting(int maintain, float desiredPaceOrSpeed) {
		SharedPreferences.Editor editor = mSettings.edit();
		if (maintain == M_PACE) {
			editor.putInt("desired_pace", (int)desiredPaceOrSpeed);
		}
		else
		if (maintain == M_SPEED) {
			editor.putFloat("desired_speed", desiredPaceOrSpeed);
		}
		editor.commit();
	}
}
