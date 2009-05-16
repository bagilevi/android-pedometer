package name.bagi.levente.pedometer;

import com.google.tts.TTS;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;


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
//    private NotificationManager mNM;

    private SharedPreferences mSettings;
    private TTS mTts;
    private SensorManager mSensorManager;
    private StepDetector mStepDetector;
    private StepBuzzer mStepBuzzer;
//    private StepNotifier mStepNotifier;
    private PaceNotifier mPaceNotifier;
    
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
    	
    	// Load settings
    	mSettings = PreferenceManager.getDefaultSharedPreferences(this);

    	// Start voice
    	mTts = new TTS(this, ttsInitListener, true);        

    	// Start detecting
        mStepDetector = new StepDetector();
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mSensorManager.registerListener(mStepDetector, 
				SensorManager.SENSOR_ACCELEROMETER | 
				SensorManager.SENSOR_MAGNETIC_FIELD | 
				SensorManager.SENSOR_ORIENTATION,
				SensorManager.SENSOR_DELAY_FASTEST);
		mStepBuzzer = new StepBuzzer(this);
		mPaceNotifier = new PaceNotifier(mPaceListener, mSettings, mTts);
		mStepDetector.addStepListener(mStepBuzzer);
		mStepDetector.addStepListener(mStepDisplayer);
		mStepDetector.addStepListener(mPaceNotifier);
		
		// Tell the user we stopped.
        Toast.makeText(this, "Service created", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onStart(Intent intent, int startId) {
    	super.onStart(intent, startId);

    	Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	// Stop detecting
    	mSensorManager.unregisterListener(mStepDetector);
    	
    	// Stop voice
    	mTts.shutdown();
    	
        // Tell the user we stopped.
        Toast.makeText(this, "Service destroyed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new StepBinder();

    public interface ICallback {
    	public void stepsChanged(int value);
    	public void paceChanged(int value);
    }
    
    private ICallback mCallback;

    public void registerCallback(ICallback cb) {
    	mCallback = cb;
    }
    
    int mDesiredPace;
    public void setDesiredPace(int desiredPace) {
    	mDesiredPace = desiredPace;
    	if (mPaceNotifier != null) {
    		mPaceNotifier.setDesiredPace(mDesiredPace);
    	}
    }
    
    private StepListener mStepDisplayer = new StepListener() {
    	private int mCount = 0;
    	public void onStep() {
    		mCount ++;
    		if (mCallback != null) {
    			mCallback.stepsChanged(mCount);
    		}
    	}
    };
    
    private PaceNotifier.Listener mPaceListener = new PaceNotifier.Listener() {
    	public void paceChanged(int value) {
    		if (mCallback != null) {
    			mCallback.paceChanged(value);
    		}
    	}
    };
    
	private TTS.InitListener ttsInitListener = new TTS.InitListener() {
		public void onInit(int version) {
		}
	};
    

}

