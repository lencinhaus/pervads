package it.polimi.dei.dbgroup.pedigree.pervads.client.android.preference;

import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Logger;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class Settings {
	public String AUTOUPDATE_KEY = null;
	public String UPDATEINTERVAL_KEY = null;
	private boolean AUTOUPDATE_DEFAULT = false;
	private int UPDATEINTERVAL_DEFAULT = 10;
	private final Logger L = new Logger(Settings.class.getSimpleName());
	private Context context;

	public Settings(Context context) {
		// setup settings keys and defaults
		if (Logger.V)
			L.v("ctor(" + context + ")");
		if (Logger.D)
			L
					.d("initializing settings keys and defaults and retrieving shared preferences");
		AUTOUPDATE_KEY = context.getString(R.string.setting_autoupdate);
		UPDATEINTERVAL_KEY = context.getString(R.string.setting_updateinterval);
		AUTOUPDATE_DEFAULT = Boolean.parseBoolean(context
				.getString(R.string.setting_autoupdate_defaultvalue));
		try {
			UPDATEINTERVAL_DEFAULT = Integer.parseInt(context
					.getString(R.string.setting_updateinterval_defaultvalue));
		} catch (NumberFormatException ex) {
			if (Logger.W)
				L.w("cannot parse updateinterval default value from strings",
						ex);
		}

		// we don't get shared preferences now because changes to preferences
		// are not
		// propagated across processes
		// get shared preferences
		// preferences = PreferenceManager.getDefaultSharedPreferences(context);

		this.context = context;
	}

	public boolean getAutoUpdate() {
		return getPreferences().getBoolean(AUTOUPDATE_KEY, AUTOUPDATE_DEFAULT);
	}

	public void setAutoUpdate(boolean newValue) {
		getPreferences().edit().putBoolean(AUTOUPDATE_KEY, newValue).commit();
	}

	public int getUpdateInterval() {
		return getPreferences().getInt(UPDATEINTERVAL_KEY,
				UPDATEINTERVAL_DEFAULT);
	}

	public void setUpdateInterval(int newValue) {
		getPreferences().edit().putInt(UPDATEINTERVAL_KEY, newValue).commit();
	}

	public SharedPreferences getPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
}
