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

import com.google.tts.TTS;

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


public class Pedometer extends Activity {
    
   
    private SharedPreferences mSettings;
    private TextView mStepValueView;
    private TextView mPaceValueView;
    TextView mDesiredPaceView;
    private int mStepValue;
    private int mPaceValue;
	private int mDesiredPace;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mStepValue = 0;
        mPaceValue = 0;
        
        setContentView(R.layout.main);
        
    	startService(new Intent(Pedometer.this,
    			StepService.class));

    }

    @Override
    protected void onResume() {
        super.onResume();

        mSettings = PreferenceManager.getDefaultSharedPreferences(this);

        if (mSettings.getBoolean("desired_pace_enabled", true) && mSettings.getBoolean("desired_pace_voice", false)) {
        	ensureTtsInstalled();
        }
        
        bindStepService();
        
        mDesiredPace = mSettings.getInt("desired_pace", 180);
        
        mStepValueView = (TextView) findViewById(R.id.step_value);
        mPaceValueView = (TextView) findViewById(R.id.pace_value);
        mDesiredPaceView = (TextView) findViewById(R.id.desired_pace_value);

        /*((TextView) this.findViewById(R.id.pace_value)).setVisibility(
	        	mSettings.getBoolean("pace_enabled", true)
	        	? View.VISIBLE
	        	: View.GONE
        );
        ((TextView) this.findViewById(R.id.pace_units)).setVisibility(
	        	mSettings.getBoolean("pace_enabled", true)
	        	? View.VISIBLE
	        	: View.GONE
        );*/
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
        
        mStepValueView.setText("" + mStepValue);
        mPaceValueView.setText("" + mPaceValue);
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
            mService = ((StepService.StepBinder)service).getService();

            mService.registerCallback(mCallback);
            mService.setDesiredPace(180); // mDesiredPace);
            mService.reloadSettings();
            
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };
    
    private void bindStepService() {
    	bindService(new Intent(Pedometer.this, 
    			StepService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindStepService() {
		unbindService(mConnection);
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
        menu.add(0, MENU_SETTINGS, 0, R.string.settings)
        	.setIcon(android.R.drawable.ic_menu_preferences)
        	.setShortcut('0', 'p')
        	.setIntent(new Intent(this, Settings.class));
    	menu.add(0, MENU_QUIT, 0, R.string.quit)
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
                	mStepValue = (int)msg.arg1;
                	mStepValueView.setText("" + mStepValue);
                    break;
                case PACE_MSG:
                	mPaceValue = msg.arg1;
					if (mPaceValue <= 0) { 
						mPaceValueView.setText("0");
					}
					else {
						mPaceValueView.setText("" + (int)mPaceValue);
					}
                	break;
                default:
                    super.handleMessage(msg);
            }
        }
        
    };
    
    private void ensureTtsInstalled() {
		TTS t = new TTS(this, null, true);
		t.shutdown();
    }
    
}