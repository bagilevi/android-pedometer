package name.bagi.levente.pedometer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class Pedometer extends Activity {
    
   
    private SharedPreferences mSettings;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);

        setContentView(R.layout.main);
        
        if (mSettings.getBoolean("desired_pace_voice", false)) {
//        	mTts = new TTS(this, ttsInitListener, true);
        }

        startStepService();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        
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
//        mStepDetector.setSensitivity(
//        		Integer.parseInt(mSettings.getString("sensitivity", "30"))
//        	);
        
    }

    @Override
    protected void onStop() {
//        mSensorManager.unregisterListener(mStepDetector);
        stopStepService();
        super.onStop();
    }
    
    private StepService mBoundService;
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((StepService.StepBinder)service).getService();
            
            // Tell the user about this for our demo.
            Toast.makeText(Pedometer.this, "Connected",
                    Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
            Toast.makeText(Pedometer.this, "Disconnected",
                    Toast.LENGTH_SHORT).show();
        }
    };
    
    private void startStepService() {
    	bindService(new Intent(Pedometer.this, 
    			StepService.class), mConnection, Context.BIND_AUTO_CREATE);
//    	startService(new Intent(Pedometer.this,
//                StepService.class));
//        startService(new Intent(
//        	"name.bagi.levente.pedometer.STEP_SERVICE"));
    }

    private void stopStepService() {
    	unbindService(mConnection);
//    	stopService(new Intent(Pedometer.this,
//                StepService.class));
//        stopService(new Intent(
//        	"name.bagi.levente.pedometer.STEP_SERVICE"));
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
    
}