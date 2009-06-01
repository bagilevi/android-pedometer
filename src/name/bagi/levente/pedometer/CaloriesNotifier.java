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

import com.google.tts.TTS;

/**
 * Calculates and displays the distance walked.  
 * @author Levente Bagi
 */
public class CaloriesNotifier implements StepListener {

	public interface Listener {
		public void valueChanged(int value);
		public void passValue();
	}
	private Listener mListener;
	
	int mCounter = 0;
	float mDistance = 0;
	
    PedometerSettings mSettings;
    TTS mTts;
    
    boolean mIsMetric;
    float mStepLength;

    // TODO: pass PedometerSettings
	public CaloriesNotifier(Listener listener, PedometerSettings settings, TTS tts) {
		mListener = listener;
		mTts = tts;
		mSettings = settings;
		reloadSettings();
	}
	public void reloadSettings() {
		mIsMetric = mSettings.isMetric();
		mStepLength = mSettings.getStepLength();
		notifyListener();
	}
	
	public void setTts(TTS tts) {
		mTts = tts;
	}
	public void isMetric(boolean isMetric) {
		mIsMetric = isMetric;
	}
	public void setStepLength(float stepLength) {
		mStepLength = stepLength;
	}
	
	public void onStep() {
		mCounter ++;
		
		if (mIsMetric) {
			mDistance = // kilometers
				mCounter * mStepLength // centimeters
				/ 100000; // centimeters/kilometer
		}
		else {
			mDistance = // miles
				mCounter * mStepLength // inches
				/ 63360; // inches/mile 
		}
		
		notifyListener();
	}
	
	private void notifyListener() {
		mListener.valueChanged((int)mDistance);
	}
	
	public void passValue() {
		
	}
	

}

