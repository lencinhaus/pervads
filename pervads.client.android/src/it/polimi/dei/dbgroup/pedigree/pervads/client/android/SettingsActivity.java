package it.polimi.dei.dbgroup.pedigree.pervads.client.android;

import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Logger;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
	private static final Logger L = new Logger(SettingsActivity.class
			.getSimpleName());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// add settings preferences
		if (Logger.D)
			L.d("adding preferences from xml resource");
		addPreferencesFromResource(R.xml.settings);
	}
}
