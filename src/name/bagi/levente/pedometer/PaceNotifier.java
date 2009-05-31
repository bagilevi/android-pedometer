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

import com.google.tts.TTS;

import android.content.SharedPreferences;

/**
 * Calculates and displays pace (steps / minute), handles input of desired pace,
 * notifies user if he/she has to go faster or slower.  
 * @author Levente Bagi
 */
public class PaceNotifier implements StepListener {

	public interface Listener {
		public void paceChanged(int value);
		public void passValue();
	}
    private ArrayList<Listener> mListeners = new ArrayList<Listener>();
	
	int mCounter = 0;
	int mDesiredPace;
	
	private long mLastStepTime = 0;
	private long[] mLastStepDeltas = {-1, -1, -1, -1};
	private int mLastStepDeltasIndex = 0;
	private long mPace = 0;
	
    private long mSpokenAt = 0;
    
    SharedPreferences mSettings;
    TTS mTts;

	public PaceNotifier(SharedPreferences settings, TTS tts) {
		mTts = tts;
		mSettings = settings;
		mDesiredPace = mSettings.getInt("desired_pace", 180);
		
		notifyListener();
	}
	
	public void addListener(Listener l) {
		mListeners.add(l);
	}

	public void setDesiredPace(int desiredPace) {
		mDesiredPace = desiredPace;
	}
	
	public void setTts(TTS tts) {
		mTts = tts;
	}
	
	public void onStep() {
		mCounter ++;
		
		// Calculate pace based on last x steps
		if (mLastStepTime > 0) {
			long now = System.currentTimeMillis();
			long delta = now - mLastStepTime;
			
			mLastStepDeltas[mLastStepDeltasIndex] = delta;
			mLastStepDeltasIndex = (mLastStepDeltasIndex + 1) % mLastStepDeltas.length;
			
			long sum = 0;
			boolean isMeaningfull = true;
			for (int i = 0; i < mLastStepDeltas.length; i++) {
				if (mLastStepDeltas[i] < 0) {
					isMeaningfull = false;
					break;
				}
				sum += mLastStepDeltas[i];
			}
			if (isMeaningfull) {
				long avg = sum / mLastStepDeltas.length;
				mPace = 60*1000 / avg;

				if (mTts != null) {
    				if (now - mSpokenAt > 3000) {
    					float little = 0.10f;
    					float normal = 0.30f;
    					float much = 0.50f;
    					
    					boolean spoken = true;
	    				if (mPace < mDesiredPace * (1 - much)) {
	    					mTts.speak("much faster!", 0, null);
	    				}
	    				else
	    				if (mPace > mDesiredPace * (1 + much)) {
	    					mTts.speak("much slower!", 0, null);
	    				}
	    				else
	    				if (mPace < mDesiredPace * (1 - normal)) {
	    					mTts.speak("faster!", 0, null);
	    				}
	    				else
	    				if (mPace > mDesiredPace * (1 + normal)) {
	    					mTts.speak("slower!", 0, null);
	    				}
	    				else
	    				if (mPace < mDesiredPace * (1 - little)) {
	    					mTts.speak("a little faster!", 0, null);
	    				}
	    				else
	    				if (mPace > mDesiredPace * (1 + little)) {
	    					mTts.speak("a little slower!", 0, null);
	    				}
	    				else
	    				if (now - mSpokenAt > 15000) {
	    					mTts.speak("You're doing great!", 0, null);
	    				}
	    				else {
	    					spoken = false;
	    				}
	    				if (spoken) {
	    					mSpokenAt = now;
	    				}
    				}
				}
			}
			else {
				mPace = -1;
			}
		}
		mLastStepTime = System.currentTimeMillis();
		notifyListener();
	}
	
	private void notifyListener() {
		for (Listener listener : mListeners) {
			listener.paceChanged((int)mPace);
		}
	}
	
	public void passValue() {
		
	}
	

}

