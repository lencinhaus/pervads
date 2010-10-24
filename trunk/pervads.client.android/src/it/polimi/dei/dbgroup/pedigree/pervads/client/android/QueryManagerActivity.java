package it.polimi.dei.dbgroup.pedigree.pervads.client.android;

import it.polimi.dei.dbgroup.pedigree.pervads.client.android.R;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.query.Query;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.query.QueryManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class QueryManagerActivity extends ListActivity {
	private static final int ADD_QUERY_REQUEST = 0;
	private static final int EDIT_QUERY_REQUEST = 1;
	private static final int DIALOG_CLEAR_QUERIES_CONFIRM = 0;
	private static final String EDIT_QUERY_ID_STATE_KEY = "it.polimi.dei.dbgroup.pedigree.pervads.QueryManagerActivity.EditQueryId";
	private QueryAdapter queryAdapter;
	private QueryManager queryManager;
	private int editQueryId = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.query_manager_activity);

		queryManager = new QueryManager(this);

		queryAdapter = new QueryAdapter(this, queryManager.getQueries());
		setListAdapter(queryAdapter);
		registerForContextMenu(getListView());

		if (savedInstanceState != null) {
			// restore the edited query id
			editQueryId = savedInstanceState
					.getInt(EDIT_QUERY_ID_STATE_KEY, -1);
		}
	}
	
	protected Dialog onCreateDialog(int id) {
		Builder builder = new Builder(this);
		switch(id) {
			case DIALOG_CLEAR_QUERIES_CONFIRM:
				builder.setMessage(R.string.query_manager_dialog_clear_queries_confirm_message)
				.setCancelable(false)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						clearQueries();
					}
				})
				.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
					
				});
				return builder.create();
			default:
				return super.onCreateDialog(id);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// save edited query id
		outState.putInt(EDIT_QUERY_ID_STATE_KEY, editQueryId);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		toggleQueryEnablement(position);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.query_manager_context, menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (super.onCreateOptionsMenu(menu)) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.query_manager_options, menu);
			return true;
		}
		return false;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
			case R.id.delete:
				deleteQuery((int) info.id);
				return true;
			case R.id.edit:
				editQuery((int) info.id);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (!super.onOptionsItemSelected(item)) {
			switch (item.getItemId()) {
				case R.id.add:
					addQuery();
					return true;
				case R.id.clear:
					showDialog(DIALOG_CLEAR_QUERIES_CONFIRM);
					return true;
			}
			return false;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ADD_QUERY_REQUEST) {
			if (resultCode == Activity.RESULT_OK) {
				Query query = data
						.getParcelableExtra(QueryEditorActivity.QUERY_EXTRA);
				queryAdded(query);
			}
		} else if (requestCode == EDIT_QUERY_REQUEST) {
			if (resultCode == Activity.RESULT_OK) {
				Query query = data
						.getParcelableExtra(QueryEditorActivity.QUERY_EXTRA);
				queryUpdated(query);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void deleteQuery(int id) {
		queryManager.getQueries().remove(id);
		queryManager.writeQueries();
		queryAdapter.notifyDataSetChanged();
	}

	private void editQuery(int id) {
		editQueryId = id;
		Query query = queryManager.getQueries().get(id);
		Intent intent = new Intent(this, QueryEditorActivity.class);
		intent.putExtra(QueryEditorActivity.QUERY_EXTRA, (Parcelable) query);
		startActivityForResult(intent, EDIT_QUERY_REQUEST);
	}

	private void addQuery() {
		startActivityForResult(new Intent(this, QueryEditorActivity.class),
				ADD_QUERY_REQUEST);
	}

	private void clearQueries() {
		queryManager.getQueries().clear();
		queryManager.writeQueries();
		queryAdapter.notifyDataSetChanged();
	}

	private void toggleQueryEnablement(int id) {
		Query query = queryManager.getQueries().get(id);
		query.setEnabled(!query.isEnabled());
		queryManager.writeQueries();
		queryAdapter.notifyDataSetChanged();

		// show some feedback to the user
		Toast
				.makeText(
						this,
						Html
								.fromHtml(getString(
										query.isEnabled() ? R.string.query_manager_toast_query_enabled
												: R.string.query_manager_toast_query_disabled,
										query.getName())), Toast.LENGTH_SHORT)
				.show();
	}

	private void queryAdded(Query query) {
		queryManager.getQueries().add(query);
		queryManager.writeQueries();
		queryAdapter.notifyDataSetChanged();
	}

	private void queryUpdated(Query query) {
		if (editQueryId != -1) {
			queryManager.getQueries().remove(editQueryId);
			queryManager.getQueries().add(editQueryId, query);
			queryManager.writeQueries();
			queryAdapter.notifyDataSetChanged();
			editQueryId = -1;
		}
	}
}
