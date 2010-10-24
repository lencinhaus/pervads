package it.polimi.dei.dbgroup.pedigree.pervads.client.android.util;

import android.content.Context;

public interface Initializable {
	public boolean isInitialized(Context context);
	
	public void initialize(Context context, ProgressMonitor monitor);
}
