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

/**
 * Calculates and displays pace (steps / minute), handles input of desired pace,
 * notifies user if he/she has to go faster or slower.  
 * @author Levente Bagi
 */
public class PaceNotifier implements StepListener, SpeakingTimer.Listener {

    public interface Listener {
        public void paceChanged(int value);
        public void passValue();
    }
    private ArrayList<Listener> mListeners = new ArrayList<Listener>();
    
    int mCounter = 0;
    
    private long mLastStepTime = 0;
    private long[] mLastStepDeltas = {-1, -1, -1, -1};
    private int mLastStepDeltasIndex = 0;
    private long mPace = 0;
    
    PedometerSettings mSettings;
    TTS mTts;

    /** Desired pace, adjusted by the user */
    int mDesiredPace;

    /** Should we speak? */
    boolean mShouldTellFasterslower;

    /** When did the TTS speak last time */
    private long mSpokenAt = 0;

    public PaceNotifier(PedometerSettings settings, TTS tts) {
        mTts = tts;
        mSettings = settings;
        mDesiredPace = mSettings.getDesiredPace();
        reloadSettings();
    }
    public void setPace(int pace) {
        mPace = pace;
        int avg = (int)(60*1000.0 / mPace);
        for (int i = 0; i < mLastStepDeltas.length; i++) {
            mLastStepDeltas[i] = avg;
        }
        notifyListener();
    }
    public void reloadSettings() {
        mShouldTellFasterslower = 
            mSettings.shouldTellFasterslower()
            && mSettings.getMaintainOption() == PedometerSettings.M_PACE;
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
                
                // TODO: remove duplication. This also exists in SpeedNotifier
                if (mShouldTellFasterslower && mTts != null) {
                    if (now - mSpokenAt > 3000 && !mTts.isSpeaking()) {
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
        // Not used
    }

    //-----------------------------------------------------
    // Speaking
    
    public void speak() {
        if (mSettings.shouldTellPace() && mTts != null) {
            if (mPace > 0) {
                mTts.speak(mPace + " steps per minute", 1, null);
            }
        }
    }
    

}

