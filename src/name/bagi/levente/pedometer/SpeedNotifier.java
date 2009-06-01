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
 * Calculates and displays pace (steps / minute), handles input of desired pace,
 * notifies user if he/she has to go faster or slower.  
 * @author Levente Bagi
 */
public class SpeedNotifier implements PaceNotifier.Listener {

	public interface Listener {
		public void valueChanged(float value);
		public void passValue();
	}
	private Listener mListener;
	
	int mCounter = 0;
	float mSpeed = 0;
	
    boolean mIsMetric;
    float mStepLength;

    PedometerSettings mSettings;
    TTS mTts;

    // TODO: pass PedometerSettings
	public SpeedNotifier(Listener listener, PedometerSettings settings, TTS tts) {
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
	
	private void notifyListener() {
		mListener.valueChanged(mSpeed);
	}
	
	public void passValue() {
		
	}

	@Override
	public void paceChanged(int value) {
		if (mIsMetric) {
			mSpeed = // kilometers / hour
				value * mStepLength // centimeters / minute
				/ 100000f * 60f; // centimeters/kilometer
		}
		else {
			mSpeed = // miles / hour
				value * mStepLength // inches / minute
				/ 63360f * 60f; // inches/mile 
		}
		notifyListener();
	}
	

}

