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

import com.google.tts.TTS;

public class Pedometer extends Activity {
    
	private SensorManager mSensorManager;
	private StepDetector mStepDetector;
    private StepNotifier mStepNotifier;
    private PaceNotifier mPaceNotifier;
    
    private TTS mTts;
    
    private SharedPreferences mSettings;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);

        setContentView(R.layout.main);
        
        if (mSettings.getBoolean("desired_pace_voice", false)) {
        	mTts = new TTS(this, ttsInitListener, true);
        }

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mPaceNotifier = new PaceNotifier(this, mSettings, mTts);
        mStepNotifier = new StepNotifier(this);
        mStepDetector = new StepDetector();
        mStepDetector.addStepListener(mStepNotifier);
        mStepDetector.addStepListener(mPaceNotifier);
        
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
        mStepDetector.setSensitivity(
        		Integer.parseInt(mSettings.getString("sensitivity", "30"))
        	);
        
    }

    @Override
    protected void onStop() {
        mSensorManager.unregisterListener(mStepDetector);
        super.onStop();
    }

    private static final int MENU_SETTINGS = 1;
    
    /* Creates the menu items */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_SETTINGS, 0, "Settings")
        	.setIcon(android.R.drawable.ic_menu_preferences)
        	.setShortcut('0', 'p')
        	.setIntent(new Intent(this, Settings.class));
        return true;
    }

    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }
    
}