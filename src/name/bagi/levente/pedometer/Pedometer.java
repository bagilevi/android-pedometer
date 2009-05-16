package name.bagi.levente.pedometer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class Pedometer extends Activity {
    
   
    private SharedPreferences mSettings;
    private TextView mStepCountView;
    private TextView mPaceValueView;
    private TextView mDesiredPaceView;
    private int mStepCount;
    private int mPace;
	private int mDesiredPace;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mStepCount = 0;
        mPace = 0;
        
        setContentView(R.layout.main);
        
    	startService(new Intent(Pedometer.this,
    			StepService.class));

    }

    @Override
    protected void onResume() {
        super.onResume();

        bindStepService();
        
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mDesiredPace = mSettings.getInt("desired_pace", 180);
        
        mStepCountView = (TextView) findViewById(R.id.step_count);
        mPaceValueView = (TextView) findViewById(R.id.pace_value);
        mDesiredPaceView = (TextView) findViewById(R.id.desired_pace_value);

        ((TextView) this.findViewById(R.id.pace_value)).setVisibility(
	        	mSettings.getBoolean("pace_enabled", true)
	        	? View.VISIBLE
	        	: View.GONE
        );
        ((TextView) this.findViewById(R.id.pace_units)).setVisibility(
	        	mSettings.getBoolean("pace_enabled", true)
	        	? View.VISIBLE
	        	: View.GONE
        );
        ((LinearLayout) this.findViewById(R.id.desired_pace_control)).setVisibility(
        		mSettings.getBoolean("pace_enabled", true)
        		&&
        		mSettings.getBoolean("desired_pace_enabled", false)
            	? View.VISIBLE
            	: View.GONE
            );
        
		Button button1 = (Button) findViewById(R.id.button_desired_pace_lower);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	mDesiredPace -= 10;
            	mDesiredPaceView.setText("" + mDesiredPace);
            	setDesiredPace(mDesiredPace);
            }
        });
        Button button2 = (Button) findViewById(R.id.button_desired_pace_raise);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	mDesiredPace += 10;
            	mDesiredPaceView.setText("" + mDesiredPace);
            	setDesiredPace(mDesiredPace);
            }
        });
        
        mStepCountView.setText("" + mStepCount);
        mPaceValueView.setText("" + mPace);
        mDesiredPaceView.setText("" + mDesiredPace);
    }
    
    @Override
    protected void onPause() {
    	unbindStepService();
    	super.onPause();
    	savePaceSetting();
    }

    @Override
    protected void onStop() {
//        mSensorManager.unregisterListener(mStepDetector);
        super.onStop();
    }

    protected void onDestroy() {
    	super.onDestroy();
    }
    
    private void setDesiredPace(int desiredPace) {
    	if (mService != null) {
    		mService.setDesiredPace(mDesiredPace);
    	}
    }
    
    private void savePaceSetting() {
		SharedPreferences.Editor editor = mSettings.edit();
		editor.putInt("desired_pace", mDesiredPace);
		editor.commit();
	}

    private StepService mService;
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mService = ((StepService.StepBinder)service).getService();

            // We want to monitor the service for as long as we are
            // connected to it.
            mService.registerCallback(mCallback);
            mService.setDesiredPace(mDesiredPace);
            mService.reloadSettings();
            
            // Tell the user about this for our demo.
            Toast.makeText(Pedometer.this, "Connected",
                    Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mService = null;
            Toast.makeText(Pedometer.this, "Disconnected",
                    Toast.LENGTH_SHORT).show();
        }
    };
    
    private void bindStepService() {
    	bindService(new Intent(Pedometer.this, 
    			StepService.class), mConnection, Context.BIND_AUTO_CREATE);
//        startService(new Intent(
//        	"name.bagi.levente.pedometer.STEP_SERVICE"));
    }

    private void unbindStepService() {
		unbindService(mConnection);
//    	
//        stopService(new Intent(
//        	"name.bagi.levente.pedometer.STEP_SERVICE"));
    }
    
    private void stopStepService() {
    	if (mService != null) {
    		stopService(new Intent(Pedometer.this,
                  StepService.class));
    	}
    }

    private static final int MENU_SETTINGS = 1;
    private static final int MENU_QUIT     = 2;
    
    /* Creates the menu items */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_SETTINGS, 0, "Settings")
        	.setIcon(android.R.drawable.ic_menu_preferences)
        	.setShortcut('0', 'p')
        	.setIntent(new Intent(this, Settings.class));
    	menu.add(0, MENU_QUIT, 0, "Quit")
    		.setIcon(android.R.drawable.ic_lock_power_off)
    		.setShortcut('9', 'q');
        return true;
    }

    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case MENU_QUIT:
    			stopStepService();
    			finish();
    			return true;
    	}
        return false;
    }
 
    
    private StepService.ICallback mCallback = new StepService.ICallback() {
    	public void stepsChanged(int value) {
        	mHandler.sendMessage(mHandler.obtainMessage(STEPS_MSG, value, 0));
        }
    	public void paceChanged(int value) {
    		mHandler.sendMessage(mHandler.obtainMessage(PACE_MSG, value, 0));
    	}
    };
    
    private static final int STEPS_MSG = 1;
    private static final int PACE_MSG = 2;
    
    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case STEPS_MSG:
                	mStepCount = (int)msg.arg1;
                	mStepCountView.setText("" + mStepCount);
                    break;
                case PACE_MSG:
                	mPace = msg.arg1;
					if (mPace <= 0) { 
						mPaceValueView.setText("0");
					}
					else {
						mPaceValueView.setText("" + (int)mPace);
					}
                	break;
                default:
                    super.handleMessage(msg);
            }
        }
        
    };
    
    
}