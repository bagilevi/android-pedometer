package name.bagi.levente.pedometer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
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
    private NotificationManager mNM;

    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;

    private SharedPreferences mSettings;
    private TTS mTts;
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
        
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mTts = new TTS(this, ttsInitListener, true);        
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
    	mTts.speak("started", 0, null);
    	Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent.getExtras();
        mServiceHandler.sendMessage(msg);
    }

    @Override
    public void onDestroy() {
    	mTts.speak("exiting", 0, null);
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

    /**
     * Show a notification while this service is running.
     */
    private void showNumber(int number) {
    	Toast.makeText(this, "Number: " + number, Toast.LENGTH_SHORT).show();
    }
    
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        
        @Override
        public void handleMessage(Message msg)
        {
//            Bundle arguments = (Bundle)msg.obj;
//            String txt = "valami string";
//            txt = txt + arguments.getString("name");
//        
//            Log.i("ServiceStartArguments", "Message: " + msg + ", " + txt);
        
            int number = 0;
            
            
        
        // Normally we would do some work here...  for our sample, we will
        // just sleep for 10 seconds.
            long endTime = System.currentTimeMillis() + 30*1000;
            while (System.currentTimeMillis() < endTime) {
                synchronized (this) {
                    try {
                        wait(3000);
                        showNumber(++ number);
                    } catch (Exception e) {
                    }
                }
            }
        
            //Log.i("ServiceStartArguments", "Done with #" + msg.arg1);
            stopSelf(msg.arg1);
        }

    };

    
	private TTS.InitListener ttsInitListener = new TTS.InitListener() {
		public void onInit(int version) {
		}
	};
    

}

