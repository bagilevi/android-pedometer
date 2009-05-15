package name.bagi.levente.pedometer;

import com.google.tts.TTS;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Calculates and displays pace (steps / minute), handles input of desired pace,
 * notifies user if he/she has to go faster or slower.  
 * @author Levente Bagi
 */
public class PaceNotifier implements StepListener {

	int mCounter = 0;
	
	private long mLastStepTime = 0;
	private long[] mLastStepDeltas = {-1, -1, -1, -1};
	private int mLastStepDeltasIndex = 0;
	private long mPace = -1;
//	private TextView mPaceValue;
	
	private int mDesiredPace;
//    private TextView mDesiredPaceText;
    
    private long mSpokenAt = 0;
    
    SharedPreferences mSettings;
//    Activity mActivity;
    TTS mTts;

	public PaceNotifier(Context context, SharedPreferences settings, TTS tts) {
//		mActivity = activity;
		mSettings = settings;
		mTts = tts;
		
		mDesiredPace = mSettings.getInt("desired_pace", 180);
		
//        mPaceValue = (TextView) mActivity.findViewById(R.id.pace_value);

//		Button button1 = (Button) mActivity.findViewById(R.id.button_desired_pace_lower);
//        button1.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//            	mDesiredPace -= 10;
//            	display();
//            	saveSetting();
//            }
//        });
//        Button button2 = (Button) mActivity.findViewById(R.id.button_desired_pace_raise);
//        button2.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//            	mDesiredPace += 10;
//            	display();
//            	saveSetting();
//            }
//        });
        
//        mDesiredPaceText = (TextView) mActivity.findViewById(R.id.desired_pace_value);

//        display();
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

				if (mSettings.getBoolean("desired_pace_voice", false)) {
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
//		display();
	}
	
//	private void display() {
//		if (mPace < 0) { 
//			mPaceValue.setText("?");
//		}
//		else {
//			mPaceValue.setText("" + (int)mPace);
//		}
//
//		mDesiredPaceText.setText("" + mDesiredPace);
//	}

//	private void saveSetting() {
//		SharedPreferences.Editor editor = mSettings.edit();
//		editor.putInt("desired_pace", mDesiredPace);
//		editor.commit();
//	}
}

