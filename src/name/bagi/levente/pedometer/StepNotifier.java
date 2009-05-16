package name.bagi.levente.pedometer;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

/**
 * Displays step count as it is incremented.
 * @author Levente Bagi
 */
public class StepNotifier extends TextView implements StepListener {
	
	private Activity mActivity;

	int mCounter = 0;
	private TextView mStepCount;
	
	public StepNotifier(Context context) {
		super(context);
		mActivity = (Activity)context;
		
        mStepCount = (TextView) mActivity.findViewById(R.id.step_count);
	}
	
	public void onStep() {
		// Add step
		mCounter ++;
		
		display();
	}

	public void passValue() {
		
	}

	private void display() {
		mStepCount.setText("" + mCounter);
	}
}

