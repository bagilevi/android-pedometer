package name.bagi.levente.pedometer;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


public class Pedometer extends Activity {
    
   
    private SharedPreferences mSettings;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        startStepService();

        mSettings = PreferenceManager.getDefaultSharedPreferences(this);

        setContentView(R.layout.main);
        
        if (mSettings.getBoolean("desired_pace_voice", false)) {
//        	mTts = new TTS(this, ttsInitListener, true);
        }
        
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
    
    private void startStepService() {
        startService(new Intent(Pedometer.this,
                StepService.class));
//        startService(new Intent(
//        	"name.bagi.levente.pedometer.STEP_SERVICE"));
    }

    private void stopStepService() {
        stopService(new Intent(Pedometer.this,
                StepService.class));
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