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
 * 
 * Uses {@link PaceNotifier}, calculates speed as product of pace and step length.
 * 
 * @author Levente Bagi
 */
public class SpeedNotifier implements PaceNotifier.Listener, SpeakingTimer.Listener {

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

    /** Desired speed, adjusted by the user */
    float mDesiredSpeed;
    
    /** Should we speak? */
    boolean mShouldTellFasterslower;
    boolean mShouldTellSpeed;
    
    /** When did the TTS speak last time */
    private long mSpokenAt = 0;
    
    public SpeedNotifier(Listener listener, PedometerSettings settings, TTS tts) {
        mListener = listener;
        mTts = tts;
        mSettings = settings;
        mDesiredSpeed = mSettings.getDesiredSpeed();
        reloadSettings();
    }
    public void setSpeed(float speed) {
        mSpeed = speed;
        notifyListener();
    }
    public void reloadSettings() {
        mIsMetric = mSettings.isMetric();
        mStepLength = mSettings.getStepLength();
        mShouldTellSpeed = mSettings.shouldTellSpeed();
        mShouldTellFasterslower = 
            mSettings.shouldTellFasterslower()
            && mSettings.getMaintainOption() == PedometerSettings.M_SPEED;
        notifyListener();
    }
    public void setTts(TTS tts) {
        mTts = tts;
    }
    public void setDesiredSpeed(float desiredSpeed) {
        mDesiredSpeed = desiredSpeed;
    }
    
    private void notifyListener() {
        mListener.valueChanged(mSpeed);
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
        tellFasterSlower();
        notifyListener();
    }
    
    /**
     * Say slower/faster, if needed.
     */
    private void tellFasterSlower() {
        if (mShouldTellFasterslower && mTts != null) {
            long now = System.currentTimeMillis();
            if (now - mSpokenAt > 3000 && !mTts.isSpeaking()) {
                float little = 0.10f;
                float normal = 0.30f;
                float much = 0.50f;
                
                boolean spoken = true;
                if (mSpeed < mDesiredSpeed * (1 - much)) {
                    mTts.speak("much faster!", 0, null);
                }
                else
                if (mSpeed > mDesiredSpeed * (1 + much)) {
                    mTts.speak("much slower!", 0, null);
                }
                else
                if (mSpeed < mDesiredSpeed * (1 - normal)) {
                    mTts.speak("faster!", 0, null);
                }
                else
                if (mSpeed > mDesiredSpeed * (1 + normal)) {
                    mTts.speak("slower!", 0, null);
                }
                else
                if (mSpeed < mDesiredSpeed * (1 - little)) {
                    mTts.speak("a little faster!", 0, null);
                }
                else
                if (mSpeed > mDesiredSpeed * (1 + little)) {
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
    
    public void passValue() {
        // Not used
    }

    @Override
    public void speak() {
        if (mSettings.shouldTellSpeed() && mTts != null) {
            if (mSpeed >= .01f) {
                mTts.speak(("" + (mSpeed + 0.000001f)).substring(0, 4) + (mIsMetric ? " kilometers per hour" : " miles per hour"), 1, null);
            }
        }
        
    }

}

