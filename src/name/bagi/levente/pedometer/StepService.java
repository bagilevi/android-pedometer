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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.tts.TTS;


/**
 * This is an example of implementing an application service that runs locally
 * in the same process as the application.  The {@link StepServiceController}
 * and {@link StepServiceBinding} classes show how to interact with the
 * service.
 *
 * <p>Notice the use of the {@link NotificationManager} when interesting things
 * happen in the service.  This is generally how background services should
 * interact with the user, rather than doing something more disruptive such as
 * calling startActivity().
 */
public class StepService extends Service {

    private SharedPreferences mSettings;
    private PedometerSettings mPedometerSettings;
    private TTS mTts;
    private SensorManager mSensorManager;
    private StepDetector mStepDetector;
    // private StepBuzzer mStepBuzzer; // used for debugging
    private PaceNotifier mPaceNotifier;
    private DistanceNotifier mDistanceNotifier;
    private SpeedNotifier mSpeedNotifier;
    private CaloriesNotifier mCaloriesNotifier;
    
    private PowerManager.WakeLock wakeLock;
    private NotificationManager mNM;
    
    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class StepBinder extends Binder {
        StepService getService() {
            return StepService.this;
        }
    }
    
    @Override
    public void onCreate() {
    	super.onCreate();
    	
    	mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    	showNotification();
    	
    	PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    	wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StepService");
    	wakeLock.acquire();
    	
    	// Load settings
    	mSettings = PreferenceManager.getDefaultSharedPreferences(this);
    	mPedometerSettings = new PedometerSettings(mSettings);

    	// Start detecting
        mStepDetector = new StepDetector();
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mSensorManager.registerListener(mStepDetector, 
				SensorManager.SENSOR_ACCELEROMETER | 
				SensorManager.SENSOR_MAGNETIC_FIELD | 
				SensorManager.SENSOR_ORIENTATION,
				SensorManager.SENSOR_DELAY_FASTEST);
		mPaceNotifier     = new PaceNotifier    (mPedometerSettings, mTts);
		mPaceNotifier.addListener(mPaceListener);
		mDistanceNotifier = new DistanceNotifier(mDistanceListener, mPedometerSettings, mTts);
		mSpeedNotifier    = new SpeedNotifier   (mSpeedListener,    mPedometerSettings, mTts);
		mCaloriesNotifier = new CaloriesNotifier(mCaloriesListener, mPedometerSettings, mTts);
		mStepDetector.addStepListener(mStepDisplayer);
		mStepDetector.addStepListener(mPaceNotifier);
		mStepDetector.addStepListener(mDistanceNotifier);
		mPaceNotifier.addListener(mSpeedNotifier);
		mStepDetector.addStepListener(mCaloriesNotifier);
		
		// Used when debugging:
		// mStepBuzzer = new StepBuzzer(this);
		// mStepDetector.addStepListener(mStepBuzzer);

		// Start voice
    	reloadSettings();
    	
    }
    
    @Override
    public void onStart(Intent intent, int startId) {
    	super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
    	
    	mNM.cancel(R.string.app_name);

    	wakeLock.release();
    	
    	super.onDestroy();
    	
    	// Stop detecting
    	mSensorManager.unregisterListener(mStepDetector);
    	
    	// Stop voice
    	if (mTts != null) {
    		mTts.shutdown();
    	}
    	
        // Tell the user we stopped.
        Toast.makeText(this, getText(R.string.stopped), Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
    	return mBinder;
    }

    /**
     * Receives messages from activity.
     */
    private final IBinder mBinder = new StepBinder();

    public interface ICallback {
    	public void stepsChanged(int value);
    	public void paceChanged(int value);
    	public void distanceChanged(float value);
    	public void speedChanged(float value);
    	public void caloriesChanged(int value);
    }
    
    private ICallback mCallback;

    public void registerCallback(ICallback cb) {
    	mCallback = cb;
    	mStepDisplayer.passValue();
    	mPaceListener.passValue();
    }
    
    private int mDesiredPace;
    private float mDesiredSpeed;
    
    /**
     * Called by activity to pass the desired pace value, 
     * whenever it is modified by the user.
     * @param desiredPace
     */
    public void setDesiredPace(int desiredPace) {
    	mDesiredPace = desiredPace;
    	if (mPaceNotifier != null) {
    		mPaceNotifier.setDesiredPace(mDesiredPace);
    	}
    }
    /**
     * Called by activity to pass the desired speed value, 
     * whenever it is modified by the user.
     * @param desiredSpeed
     */
    public void setDesiredSpeed(float desiredSpeed) {
    	mDesiredSpeed = desiredSpeed;
    	if (mSpeedNotifier != null) {
    		mSpeedNotifier.setDesiredSpeed(mDesiredSpeed);
    	}
    }
    
    public void reloadSettings() {
    	mSettings = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	if (mStepDetector != null) { 
	    	mStepDetector.setSensitivity(
	    			Integer.valueOf(mSettings.getString("sensitivity", "30"))
	    	);
    	}
    	
    	boolean userWantsVoice = 
    		// "maintain" is not "none"
    		mPedometerSettings.getMaintainOption() != PedometerSettings.M_NONE
    		// voice is enabled
    		&& mSettings.getBoolean("desired_pace_voice", false); // TODO: update with settings redesign
    	
    	if (mTts == null && userWantsVoice && TTS.isInstalled(this)) {
    		mTts = new TTS(this, null, false);
    		if (mPaceNotifier != null) {
    			mPaceNotifier.setTts(mTts);
    		}
    		if (mSpeedNotifier != null) {
    			mSpeedNotifier.setTts(mTts);
    		}
    	}
    	
    	if (mPaceNotifier     != null) mPaceNotifier.reloadSettings();
    	if (mDistanceNotifier != null) mDistanceNotifier.reloadSettings();
    	if (mSpeedNotifier    != null) mSpeedNotifier.reloadSettings();
    	if (mCaloriesNotifier != null) mCaloriesNotifier.reloadSettings();
    }
    
    /**
     * Counts steps provided by StepDetector and passes the current
     * step count to the activity.
     */
    private StepListener mStepDisplayer = new StepListener() {
    	private int mCount = 0;
    	public void onStep() {
    		mCount ++;
    		passValue();
    	}
    	public void passValue() {
    		if (mCallback != null) {
    			mCallback.stepsChanged(mCount);
    		}
    	}
    };
    
    /**
     * Forwards pace values from PaceNotifier to the activity. 
     */
    private PaceNotifier.Listener mPaceListener = new PaceNotifier.Listener() {
    	int currentPace = 0;
    	
    	public void paceChanged(int value) {
    		currentPace = value;
    		passValue();
    	}
    	public void passValue() {
			if (mCallback != null) {
				mCallback.paceChanged(currentPace);
			}
    	}
    };
    /**
     * Forwards distance values from DistanceNotifier to the activity. 
     */
    private DistanceNotifier.Listener mDistanceListener = new DistanceNotifier.Listener() {
    	float currentDistance = 0;
    	
    	public void valueChanged(float value) {
    		currentDistance = value;
    		passValue();
    	}
    	public void passValue() {
			if (mCallback != null) {
				mCallback.distanceChanged(currentDistance);
			}
    	}
    };
    /**
     * Forwards speed values from SpeedNotifier to the activity. 
     */
    private SpeedNotifier.Listener mSpeedListener = new SpeedNotifier.Listener() {
    	float currentSpeed = 0;
    	
    	public void valueChanged(float value) {
    		currentSpeed = value;
    		passValue();
    	}
    	public void passValue() {
			if (mCallback != null) {
				mCallback.speedChanged(currentSpeed);
			}
    	}
    };
    /**
     * Forwards calories values from CaloriesNotifier to the activity. 
     */
    private CaloriesNotifier.Listener mCaloriesListener = new CaloriesNotifier.Listener() {
    	int currentCalories = 0;
    	
    	public void valueChanged(int value) {
    		currentCalories = value;
    		passValue();
    	}
    	public void passValue() {
			if (mCallback != null) {
				mCallback.caloriesChanged(currentCalories);
			}
    	}
    };
    
    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        CharSequence text = getText(R.string.app_name);
        Notification notification = new Notification(R.drawable.ic_notification, null,
                System.currentTimeMillis());
        notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, Pedometer.class), 0);
        notification.setLatestEventInfo(this, text,
        		getText(R.string.notification_subtitle), contentIntent);

        mNM.notify(R.string.app_name, notification);
    }
}

