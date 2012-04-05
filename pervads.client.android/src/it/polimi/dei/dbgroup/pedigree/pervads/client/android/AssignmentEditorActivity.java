package it.polimi.dei.dbgroup.pedigree.pervads.client.android;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.ViewAnimator;
import android.widget.ViewFlipper;

public class AssignmentEditorActivity extends Activity {
	private static final int DIMENSION_CHOOSER_VIEW_ID = 0;
	private static final int VALUE_CHOOSER_VIEW_ID = 1;
	
	// UI elements
	private ViewAnimator viewAnimator;
	private ListView dimensionsListView;
	private ListView valuesListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.assignment_editor_activity);
		
		viewAnimator = (ViewAnimator) findViewById(R.id.animator);
		dimensionsListView = (ListView) findViewById(R.id.dimensions);
		valuesListView = (ListView) findViewById(R.id.values);
	}

	private void showForward(int viewId) {
		viewAnimator.setInAnimation(this, R.anim.slide_in_right);
		viewAnimator.setOutAnimation(this, R.anim.slide_out_left);
		viewAnimator.setDisplayedChild(viewId);
	}
}
