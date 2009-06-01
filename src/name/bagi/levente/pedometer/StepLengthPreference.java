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

import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

/**
 * An {@link EditTextPreference} that only allows float values.
 * @author Levente Bagi
 * TODO: convert the value according to the selected unit
 */
public class StepLengthPreference extends EditTextPreference {

	boolean mIsMetric;
	
	public StepLengthPreference(Context context) {
		super(context);
	}
	public StepLengthPreference(Context context, AttributeSet attr) {
		super(context, attr);
	}
	public StepLengthPreference(Context context, AttributeSet attr, int defStyle) {
		super(context, attr, defStyle);
	}
	protected void showDialog(Bundle state) {
		mIsMetric = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("units", "imperial").equals("metric");
		setDialogTitle(
				getContext().getString(R.string.step_length_setting_title) + 
				" (" + 
						getContext().getString(
								mIsMetric
								? R.string.centimeters 
								: R.string.inches) + 
				")"
		);
		
		try {
			Float.valueOf(getText());
		}
		catch (Exception e) {
			setText("20");
		}
		
		super.showDialog(state);
	}
	protected void onAddEditTextToDialogView (View dialogView, EditText editText) {
		editText.setRawInputType(
				InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		super.onAddEditTextToDialogView(dialogView, editText);
	}
	public void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			try {
				Float.valueOf(((CharSequence)(getEditText().getText())).toString());
			}
			catch (NumberFormatException e) {
				this.showDialog(null);
				return;
			}
		}
		super.onDialogClosed(positiveResult);
	}
}

