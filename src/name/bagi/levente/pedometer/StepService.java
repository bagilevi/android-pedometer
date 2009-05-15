package name.bagi.levente.pedometer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.HandlerThread;
import android.os.IBinder;
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
    private NotificationManager mNM;

//    private SharedPreferences mSettings;
//    private TTS mTts;
    private SensorManager mSensorManager;
    private StepDetector mStepDetector;
    private StepBuzzer mStepBuzzer;
//    private StepNotifier mStepNotifier;
//    private PaceNotifier mPaceNotifier;
    
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
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        HandlerThread thread = new HandlerThread("ServiceStartArguments");
        thread.start();
        
//        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
//        mTts = new TTS(this, ttsInitListener, true);        
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mStepBuzzer = new StepBuzzer(this);
//		mPaceNotifier = new PaceNotifier(this, mSettings, mTts);
		mStepDetector = new StepDetector();
		mStepDetector.addStepListener(mStepBuzzer);
//		mStepDetector.addStepListener(mPaceNotifier);

		mSensorManager.registerListener(mStepDetector, 
			SensorManager.SENSOR_ACCELEROMETER | 
			SensorManager.SENSOR_MAGNETIC_FIELD | 
			SensorManager.SENSOR_ORIENTATION,
			SensorManager.SENSOR_DELAY_FASTEST);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
    }
    
    @Override
    public void onStart(Intent intent, int startId) {
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(1);

        // Tell the user we stopped.
        Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new StepBinder();

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "Service started";

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.icon, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, Pedometer.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, "ServiceLabel",
                       text, contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNM.notify(1, notification);
    }

    
//	private TTS.InitListener ttsInitListener = new TTS.InitListener() {
//		public void onInit(int version) {
//		}
//	};
    

}

