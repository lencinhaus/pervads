package it.polimi.dei.dbgroup.pedigree.pervads.client.android;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.R;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.adapters.ValueAdapter;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.context.ContextProxyManager;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.context.ContextSearchManager;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.InitializationManager;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.InitializationManager.OnFinishListener;

import java.util.List;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class AssignmentSearchActivity extends ListActivity {
	public static final String RESTRICTED_VALUE_URIS_BUNDLE_KEY = "it.polimi.dei.dbgroup.pedigree.pervads.client.android.RestrictedValueURIs";
	// private final Logger L = new Logger(AssignmentSearchActivity.class
	// .getSimpleName());
	private static final int DIALOG_SEARCHING = 0;
	private static final int MAX_RESULTS = 100;
	private final OnFinishListener searchInitializationFinishedListener = new OnFinishListener() {

		@Override
		public void onFinish() {
			handleIntent(getIntent());
		}
	};

	private class SearchParams {
		public String query;
		public String[] restrictedValueURIs;

		protected SearchParams(String query, String[] restrictedValueURIs) {
			super();
			this.query = query;
			this.restrictedValueURIs = restrictedValueURIs;
		}

	}

	private class SearchTask extends
			AsyncTask<SearchParams, Void, List<? extends Value>> {

		@Override
		protected List<? extends Value> doInBackground(SearchParams... params) {
			SearchParams searchParams = params[0];
			List<? extends Value> values = ContextSearchManager.getInstance()
					.search(searchParams.query, MAX_RESULTS,
							AssignmentSearchActivity.this,
							searchParams.restrictedValueURIs);
			// preload parent dimension for smoother visualization
			for (Value value : values) {
				value.getParentDimension();
			}

			return values;
		}

		@Override
		protected void onPostExecute(List<? extends Value> result) {
			super.onPostExecute(result);
			valueAdapter.setValues(result);
			dismissDialog(DIALOG_SEARCHING);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			AssignmentSearchActivity.this.showDialog(DIALOG_SEARCHING);
		}

	}

	private ValueAdapter valueAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.assignment_search_activity);

		valueAdapter = new ValueAdapter(this);
		setListAdapter(valueAdapter);

		// initialize search manager
		InitializationManager.initializeAsync(this,
				searchInitializationFinishedListener, ContextProxyManager
						.getInstance(), ContextSearchManager.getInstance());
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		ContextSearchManager.getInstance().setSelectedValue(
				valueAdapter.getValue(position));
		finish();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_SEARCHING:
				ProgressDialog d = new ProgressDialog(this);
				d.setIndeterminate(true);
				d.setTitle(R.string.assignment_search_dialog_searching_title);
				d
						.setMessage(getText(R.string.assignment_search_dialog_searching_message));
				d.setCancelable(false);
				return d;
			default:
				return super.onCreateDialog(id);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (intent != null) {
			if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
				String query = intent.getStringExtra(SearchManager.QUERY);
				String[] restrictedValueUris = new String[0];
				Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
				if (appData != null) {
					restrictedValueUris = appData
							.getStringArray(RESTRICTED_VALUE_URIS_BUNDLE_KEY);
				}
				SearchTask task = new SearchTask();
				task.execute(new SearchParams(query, restrictedValueUris));
				return;
			}
		}

		onSearchRequested();
	}
}
