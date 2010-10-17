package it.polimi.dei.dbgroup.pedigree.pervads.util;

import android.content.Context;

public interface Initializable {
	public boolean isInitialized(Context context);
	
	public void initialize(Context context, ProgressMonitor monitor);
}
