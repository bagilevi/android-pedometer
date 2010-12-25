/*
 *  Pedometer - Android App
 *  Copyright (C) 2009 Levente Bagi
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
    
    public float getBodyWeight() {
        try {
            return Float.valueOf(mSettings.getString("body_weight", "50").trim());
        }
        catch (NumberFormatException e) {
            // TODO: reset value, & notify user somehow
            return 0f;
        }
    }

    public boolean isRunning() {
        return mSettings.getString("exercise_type", "running").equals("running");
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
    
    //-------------------------------------------------------------------
    // Speaking:
    
    public boolean shouldSpeak() {
        return mSettings.getBoolean("speak", false);
    }
    public float getSpeakingInterval() {
        try {
            return Float.valueOf(mSettings.getString("speaking_interval", "1"));
        }
        catch (NumberFormatException e) {
            // This could not happen as the value is selected from a list.
            return 1;
        }
    }
    public boolean shouldTellSteps() {
        return mSettings.getBoolean("speak", false) 
        && mSettings.getBoolean("tell_steps", false);
    }
    public boolean shouldTellPace() {
        return mSettings.getBoolean("speak", false) 
        && mSettings.getBoolean("tell_pace", false);
    }
    public boolean shouldTellDistance() {
        return mSettings.getBoolean("speak", false) 
        && mSettings.getBoolean("tell_distance", false);
    }
    public boolean shouldTellSpeed() {
        return mSettings.getBoolean("speak", false) 
        && mSettings.getBoolean("tell_speed", false);
    }
    public boolean shouldTellCalories() {
        return mSettings.getBoolean("speak", false) 
        && mSettings.getBoolean("tell_calories", false);
    }
    public boolean shouldTellFasterslower() {
        return mSettings.getBoolean("speak", false) 
        && mSettings.getBoolean("tell_fasterslower", false);
    }
    
    public boolean wakeAggressively() {
        return mSettings.getString("operation_level", "run_in_background").equals("wake_up");
    }
    public boolean keepScreenOn() {
        return mSettings.getString("operation_level", "run_in_background").equals("keep_screen_on");
    }
    
    //
    // Internal
    
    public void saveServiceRunningWithTimestamp(boolean running) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean("service_running", running);
        editor.putLong("last_seen", Utils.currentTimeInMillis());
        editor.commit();
    }
    
    public void saveServiceRunningWithNullTimestamp(boolean running) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean("service_running", running);
        editor.putLong("last_seen", 0);
        editor.commit();
    }

    public void clearServiceRunning() {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean("service_running", false);
        editor.putLong("last_seen", 0);
        editor.commit();
    }

    public boolean isServiceRunning() {
        return mSettings.getBoolean("service_running", false);
    }
    
    public boolean isNewStart() {
        // activity last paused more than 10 minutes ago
        return mSettings.getLong("last_seen", 0) < Utils.currentTimeInMillis() - 1000*60*10;
    }

}
