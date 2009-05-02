package name.bagi.levente.pedometer;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import com.google.tts.TTS;

public class Pedometer extends Activity {
    
	private SensorManager mSensorManager;
	private StepDetector mStepDetector = null;
    private StepNotifier mStepNotifier = null;
    
    private TTS mTts;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mStepNotifier = new StepNotifier(this);
        mStepDetector = new StepDetector(mStepNotifier);
        
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
    
    private class StepNotifier extends TextView implements StepListener {
    	
    	private Activity mActivity;

    	int mCounter = 0;
    	private TextView mStepCount;
    	
    	private long mLastStepTime = 0;
    	private long[] mLastStepDeltas = {-1, -1, -1, -1};
    	private int mLastStepDeltasIndex = 0;
    	private long mSpeed = -1;
    	private TextView mSpeedValue;
    	
    	public StepNotifier(Context context) {
    		super(context);
    		mActivity = (Activity)context;
    		
            mStepCount = (TextView) mActivity.findViewById(R.id.step_count);
            mSpeedValue = (TextView) mActivity.findViewById(R.id.speed_value);
    	}
    	
    	public void onStep() {
    		// Add step
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
    				mSpeed = 60*1000 / avg;

    				if (mCounter % (mLastStepDeltas.length * 4) == 0) {
    	    	          mTts.speak("" + mSpeed + " steps per minute", 0, null);
    	    		}
    			}
    			else {
    				mSpeed = -1;
    			}
    		}
			mLastStepTime = System.currentTimeMillis();
    		
    		display();
    	}
    	
    	private void display() {
    		mStepCount.setText("" + mCounter);
    		if (mSpeed < 0) { 
    			mSpeedValue.setText("?");
    		}
    		else {
    			mSpeedValue.setText("" + (int)mSpeed);
    		}
    	}
    }
	
	private interface StepListener {
    	public void onStep();
    }
    
    private class StepDetector implements SensorListener
    {
        private float   mLastValues[] = new float[3*2];
        private float   mScale[] = new float[2];
        private float   mYOffset;

        private float   mLastDirections[] = new float[3*2];
        private float   mLastExtremes[][] = { new float[3*2], new float[3*2] };
        private float   mLastDiff[] = new float[3*2];
        private int     mLastMatch = -1;
        
        private StepListener mStepListener = null;
    	
    	public StepDetector(StepListener stepListener) {
    		mStepListener = stepListener;
    		
    		int h = 480; // TODO: remove this constant
            mYOffset = h * 0.5f;
            mScale[0] = - (h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
            mScale[1] = - (h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
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
                            		mStepListener.onStep();
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