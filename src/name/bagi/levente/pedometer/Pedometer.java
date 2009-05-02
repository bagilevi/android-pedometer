package name.bagi.levente.pedometer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.tts.TTS;

public class Pedometer extends Activity {
    
	private SensorManager mSensorManager;
	private StepDetector mStepDetector;
    private StepNotifier mStepNotifier;
    private PaceNotifier mPaceNotifier;
    
    private TTS mTts;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mPaceNotifier = new PaceNotifier();
        mStepNotifier = new StepNotifier(this);
        mStepDetector = new StepDetector();
        mStepDetector.addStepListener(mStepNotifier);
        mStepDetector.addStepListener(mPaceNotifier);

        mTts = new TTS(this, ttsInitListener, true);
    }
    
    private TTS.InitListener ttsInitListener = new TTS.InitListener() {
        public void onInit(int version) {
        }
    };
    
    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mStepDetector, 
                SensorManager.SENSOR_ACCELEROMETER | 
                SensorManager.SENSOR_MAGNETIC_FIELD | 
                SensorManager.SENSOR_ORIENTATION,
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onStop() {
        mSensorManager.unregisterListener(mStepDetector);
        super.onStop();
    }
    
    /**
     * Calculates and displays pace (steps / minute), handles input of desired pace,
     * notifies user if he/she has to go faster or slower.  
     * @author Levente Bagi
     */
    private class PaceNotifier implements StepListener {

    	int mCounter = 0;
    	
    	private long mLastStepTime = 0;
    	private long[] mLastStepDeltas = {-1, -1, -1, -1};
    	private int mLastStepDeltasIndex = 0;
    	private long mPace = -1;
    	private TextView mPaceValue;
    	
    	private int mDesiredPace = 120;
        private TextView mDesiredPaceText;
        
        private long mSpokenAt = 0;

    	public PaceNotifier() {
            mPaceValue = (TextView) findViewById(R.id.speed_value);

    		Button button1 = (Button) findViewById(R.id.button_desired_pace_lower);
            button1.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                	mDesiredPace -= 10;
                	display();
                }
            });
            Button button2 = (Button) findViewById(R.id.button_desired_pace_raise);
            button2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                	mDesiredPace += 10;
                	display();
                }
            });
            
            mDesiredPaceText = (TextView) findViewById(R.id.desired_pace_value);

            display();
    	}
    	
    	public void onStep() {
    		mCounter ++;
    		
    		// Calculate speed based on last x steps
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
    			else {
    				mPace = -1;
    			}
    		}
			mLastStepTime = System.currentTimeMillis();
			display();
    	}
    	
    	private void display() {
    		if (mPace < 0) { 
    			mPaceValue.setText("?");
    		}
    		else {
    			mPaceValue.setText("" + (int)mPace);
    		}

    		mDesiredPaceText.setText("" + mDesiredPace);
    	}
    	
    }
    
    /**
     * Displays step count as it is incremented.
     * @author Levente Bagi
     */
    private class StepNotifier extends TextView implements StepListener {
    	
    	private Activity mActivity;

    	int mCounter = 0;
    	private TextView mStepCount;
    	
    	public StepNotifier(Context context) {
    		super(context);
    		mActivity = (Activity)context;
    		
            mStepCount = (TextView) mActivity.findViewById(R.id.step_count);
    	}
    	
    	public void onStep() {
    		// Add step
    		mCounter ++;
    		
    		display();
    	}
    	
    	private void display() {
    		mStepCount.setText("" + mCounter);
    	}
    }
	
    /**
     * Interface implemented by classes that can handle notifications about steps.
     * These classes can be passed to StepDetector.
     * @author Levente Bagi
     */
	private interface StepListener {
    	public void onStep();
    }
    
	/**
	 * Detects steps and notifies all listeners (that implement StepListener).
	 * @author Levente Bagi
	 */
    private class StepDetector implements SensorListener
    {
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
                        	int limit = 30;
                        	if (diff > limit) {
                            	
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

}