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

