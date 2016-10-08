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

import android.content.Context;
import android.os.Vibrator;

/**
 * Vibrates whenever a step is detected.
 * Normally, this is not attached, used for debugging purposes.
 * @author Levente Bagi
 */
public class StepBuzzer implements StepListener {
    
    private Context mContext;
    private Vibrator mVibrator;
    private PedometerSettings mSettings;
    
    public StepBuzzer(Context context, PedometerSettings settings) {
        mContext = context;
        mVibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
        mSettings = settings;
    }
    
    public void onStep() {
        buzz();
    }
    
    public void passValue() {
        
    }
    
    private void buzz() {
    	if (mSettings.isBuzzing()) {
    		mVibrator.vibrate(50);
    	}
    }
}

