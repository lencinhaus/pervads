package it.polimi.dei.dbgroup.pedigree.pervads;

import it.polimi.dei.dbgroup.pedigree.pervads.util.Logger;
import it.polimi.dei.dbgroup.pedigree.pervads.R;
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
