package name.bagi.levente.pedometer;

import java.util.ArrayList;

import com.google.tts.TTS;

/**
 * Counts steps provided by StepDetector and passes the current
 * step count to the activity.
 */
public class StepDisplayer implements StepListener, SpeakingTimer.Listener {

	private int mCount = 0;
	PedometerSettings mSettings;
	
	public StepDisplayer(PedometerSettings settings, TTS tts) {
		mTts = tts;
		mSettings = settings;
	}
	public void onStep() {
		mCount ++;
		passValue();
	}
	public void passValue() {
		notifyListener();
	}
	
	

	//-----------------------------------------------------
	// Listener
	
	public interface Listener {
		public void stepsChanged(int value);
		public void passValue();
	}
    private ArrayList<Listener> mListeners = new ArrayList<Listener>();

    public void addListener(Listener l) {
		mListeners.add(l);
	}
	public void notifyListener() {
		for (Listener listener : mListeners) {
			listener.stepsChanged((int)mCount);
		}
	}
	
	//-----------------------------------------------------
	// Speaking
	
	TTS mTts;

	public void setTts(TTS tts) {
		mTts = tts;
	}
	public void speak() {
		if (mSettings.shouldTellSteps()) { 
			if (mCount > 0) {
				mTts.speak("" + mCount + " steps", 1, null);
			}
		}
	}
	
	
}
