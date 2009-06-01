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

import java.util.ArrayList;

import name.bagi.levente.pedometer.StepListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;

/**
 * Detects steps and notifies all listeners (that implement StepListener).
 * @author Levente Bagi
 * @todo REFACTOR: SensorListener is deprecated
 */
@SuppressWarnings("deprecation")
public class StepDetector implements SensorListener
{
	private int     mLimit = 30;
    private float   mLastValues[] = new float[3*2];
    private float   mScale[] = new float[2];
    private float   mYOffset;

    private float   mLastDirections[] = new float[3*2];
    private float   mLastExtremes[][] = { new float[3*2], new float[3*2] };
    private float   mLastDiff[] = new float[3*2];
    private int     mLastMatch = -1;
    
    private ArrayList<StepListener> mStepListeners = new ArrayList<StepListener>();
	
	public StepDetector() {
		int h = 480; // TODO: remove this constant
        mYOffset = h * 0.5f;
        mScale[0] = - (h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
        mScale[1] = - (h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
	}
	
	public void setSensitivity(int sensitivity) {
		mLimit = sensitivity;
	}
	
	public void addStepListener(StepListener sl) {
		mStepListeners.add(sl);
	}
	
    @Override
	public void onSensorChanged(int sensor, float[] values) {
        synchronized (this) {
            if (sensor == SensorManager.SENSOR_ORIENTATION) {
            }
            else {
                int j = (sensor == SensorManager.SENSOR_MAGNETIC_FIELD) ? 1 : 0;
                if (j == 0) { 
                    float vSum = 0;
                    for (int i=0 ; i<3 ; i++) {
                        final float v = mYOffset + values[i] * mScale[j];
                        vSum += v;
                    }
                    int k = 0;
                    float v = vSum / 3;
                    
                    float direction = (v > mLastValues[k] ? 1 : (v < mLastValues[k] ? -1 : 0));
                    if (direction == - mLastDirections[k]) {
                    	// Direction changed
                    	int extType = (direction > 0 ? 0 : 1); // minumum or maximum?
                    	mLastExtremes[extType][k] = mLastValues[k];
                    	float diff = Math.abs(mLastExtremes[extType][k] - mLastExtremes[1 - extType][k]);

                    	if (diff > mLimit) {
                        	
                        	boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff[k]*2/3);
                        	boolean isPreviousLargeEnough = mLastDiff[k] > (diff/3);
                        	boolean isNotContra = (mLastMatch != 1 - extType);
                        	
                        	if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra) {
                        		for (StepListener stepListener : mStepListeners) {
                        			stepListener.onStep();
								}
                        		mLastMatch = extType;
                        	}
                        	else {
                        		mLastMatch = -1;
                        	}
                    	}
                    	mLastDiff[k] = diff;
                    }
                    mLastDirections[k] = direction;
                    mLastValues[k] = v;
                }
            }
        }
    }
    
    @Override
    public void onAccuracyChanged(int sensor, int accuracy) {
    	// Not used
    }
}