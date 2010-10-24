package it.polimi.dei.dbgroup.pedigree.pervads.client.android.widget;

import it.polimi.dei.dbgroup.pedigree.pervads.client.android.R;
import android.app.ProgressDialog;
import android.content.Context;

public class InitializationProgressDialog extends ProgressDialog {
	public InitializationProgressDialog(Context context) {
		super(context);
		init();
	}
	
	public InitializationProgressDialog(Context context, int theme) {
		super(context, theme);
		init();
	}
	
	private void init() {
		setCancelable(false);
		setTitle(R.string.initialization_progress_dialog_default_title);
		setMessage(getContext().getText(R.string.initialization_progress_dialog_default_message));
		setProgressStyle(STYLE_HORIZONTAL);
		setIndeterminate(true);
		setMax(1);
		setProgress(1);
	}
}
