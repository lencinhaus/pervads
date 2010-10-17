package it.polimi.dei.dbgroup.pedigree.pervads.preference;

import it.polimi.dei.dbgroup.pedigree.pervads.util.Logger;
import it.polimi.dei.dbgroup.pedigree.pervads.widget.NumberPicker;
import it.polimi.dei.dbgroup.pedigree.pervads.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * A {@link Preference} that allows for number input.
 * <p>
 * It is a subclass of {@link DialogPreference} and shows the
 * {@link NumberPicker} in a dialog. This {@link NumberPicker} can be modified
 * either programmatically via {@link #getNumberPicker()}, or through XML by
 * setting any NumberPicker attributes on the NumberPickerPreference.
 * <p>
 * This preference will store an integer into the SharedPreferences.
 * <p>
 * See {@link it.polimi.dei.dbgroup.pedigree.pervads.R.styleable#NumberPicker NumberPicker
 * Attributes}.
 * 
 * @attr ref it.polimi.dei.dbgroup.pedigree.pervads.R.styleable#NumberPickerPreference_minValue
 * @attr ref it.polimi.dei.dbgroup.pedigree.pervads.R.styleable#NumberPickerPreference_maxValue
 */
public class NumberPickerPreference extends DialogPreference {
	private static final Logger L = new Logger(NumberPickerPreference.class
			.getSimpleName());
	private NumberPicker mNumberPicker;
	private int mNumber;

	public NumberPickerPreference(Context context) {
		this(context, null);
	}

	public NumberPickerPreference(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.numberPickerPreferenceStyle);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public NumberPickerPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs);

		setDialogLayoutResource(R.layout.number_picker_preference);

		mNumberPicker = new NumberPicker(context, attrs);

		// Give it an ID so it can be saved/restored
		mNumberPicker.setId(android.R.id.edit);

		/*
		 * The preference framework and view framework both have an 'enabled'
		 * attribute. Most likely, the 'enabled' specified in this XML is for
		 * the preference framework, but it was also given to the view
		 * framework. We reset the enabled state.
		 */
		mNumberPicker.setEnabled(true);

		// check my styles
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.NumberPickerPreference, defStyle, 0);
		int mMinValue = a.getInt(R.styleable.NumberPickerPreference_minValue,
				Integer.parseInt(getContext().getString(
						R.string.numberpickerpreference_minvalue_default)));
		int mMaxValue = a.getInt(R.styleable.NumberPickerPreference_maxValue,
				Integer.parseInt(getContext().getString(
						R.string.numberpickerpreference_maxvalue_default)));
		int speed = a.getInt(R.styleable.NumberPickerPreference_speed, Integer
				.parseInt(getContext().getString(
						R.string.numberpickerpreference_speed_default)));
		a.recycle();
		mNumberPicker.setRange(mMinValue, mMaxValue);
		mNumberPicker.setSpeed(speed);
	}

	/**
	 * Saves the number to the {@link SharedPreferences}.
	 * 
	 * @param number
	 *            The number to save
	 */
	public void setNumber(int number) {
		if (Logger.V)
			L.v("setNumber(" + number + ")");
		final boolean wasBlocking = shouldDisableDependents();

		mNumber = number;

		persistInt(number);

		final boolean isBlocking = shouldDisableDependents();
		if (isBlocking != wasBlocking) {
			notifyDependencyChange(isBlocking);
		}
	}

	/**
	 * Gets the number from the {@link SharedPreferences}.
	 * 
	 * @return The current preference value.
	 */
	public int getNumber() {
		return mNumber;
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);

		NumberPicker numberPicker = mNumberPicker;
		numberPicker.setCurrent(getNumber());

		ViewParent oldParent = numberPicker.getParent();
		if (oldParent != view) {
			if (oldParent != null) {
				((ViewGroup) oldParent).removeView(numberPicker);
			}
			onAddNumberPickerToDialogView(view, numberPicker);
		}
	}

	/**
	 * Adds the NumberPicker widget of this preference to the dialog's view.
	 * 
	 * @param dialogView
	 *            The dialog view.
	 */
	protected void onAddNumberPickerToDialogView(View dialogView,
			NumberPicker numberPicker) {
		ViewGroup container = (ViewGroup) dialogView
				.findViewById(R.id.numberpicker_container);
		if (container != null) {
			container.addView(numberPicker, ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (Logger.V)
			L.v("onDialogClosed(" + positiveResult + ")");
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			int number = mNumberPicker.getCurrent();
			if (callChangeListener(number)) {
				setNumber(number);
			}
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index, 0);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		if (Logger.V)
			L
					.v("onSetInitialValue(" + restoreValue + ", "
							+ defaultValue + ")");
		setNumber(restoreValue ? getPersistedInt(mNumber)
				: (Integer) defaultValue);
	}

	/**
	 * Returns the {@link NumberPicker} widget that will be shown in the dialog.
	 * 
	 * @return The {@link NumberPicker} widget that will be shown in the dialog.
	 */
	public NumberPicker getNumberPicker() {
		return mNumberPicker;
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		if (isPersistent()) {
			// No need to save instance state since it's persistent
			return superState;
		}

		final SavedState myState = new SavedState(superState);
		myState.number = getNumber();
		return myState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state == null || !state.getClass().equals(SavedState.class)) {
			// Didn't save state for us in onSaveInstanceState
			super.onRestoreInstanceState(state);
			return;
		}

		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());
		setNumber(myState.number);
	}

	protected static class SavedState extends BaseSavedState {
		int number;

		public SavedState(Parcel source) {
			super(source);
			number = source.readInt();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(number);
		}

		public SavedState(Parcelable superState) {
			super(superState);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

}
