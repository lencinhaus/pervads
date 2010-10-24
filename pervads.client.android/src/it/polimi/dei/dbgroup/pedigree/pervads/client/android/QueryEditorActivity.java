package it.polimi.dei.dbgroup.pedigree.pervads.client.android;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Dimension;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.R;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.context.ContextProxyManager;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.context.ContextSearchManager;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.context.ContextSearchManager.OnValueSelectedListener;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.query.LightweightAssignment;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.query.Query;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.InitializationManager;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.InitializationManager.OnFinishListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class QueryEditorActivity extends ListActivity {
	public static final String QUERY_EXTRA = "it.polimi.dei.dbgroup.pedigree.pervads.QueryEditorActivity.Query";
	private static final int DIALOG_CLEAR_ASSIGNMENTS_CONFIRM = 0;
	private static final int DIALOG_REPLACE_ASSIGNMENT = 1;
	private static final int ADD_ASSIGNMENT_BY_NAVIGATION_REQUEST = 0;
	private static final int EDIT_ASSIGNMENT_REQUEST = 2;
	private static final String EDIT_ASSIGNMENT_ID_STATE_KEY = "it.polimi.dei.dbgroup.pedigree.pervads.QueryEditorActivity.EditAssignmentId";
	// private final Logger L = new Logger(QueryEditorActivity.class
	// .getSimpleName());
	private final OnFinishListener contextInitializationFinishedListener = new OnFinishListener() {
		@Override
		public void onFinish() {
			initializeAssignmentAdapter();
			ContextSearchManager.getInstance().setOnValueSelectedListener(
					onValueSelectedListener);
			setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		}
	};

	private final OnValueSelectedListener onValueSelectedListener = new OnValueSelectedListener() {

		@Override
		public void onValueSelected(Value value) {
			checkSelectedValue(value);
		}
	};

	private EditText nameText;
	private AssignmentAdapter assignmentAdapter;
	private Button okButton;
	private Query query;
	private int editAssignmentId = -1;
	private int conflictingAssignmentId = -1;
	private Value conflictingValue = null;
	private Value pendingValue = null;
	private Dimension conflictingRootDimension = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		createUI();

		Intent intent = getIntent();
		if (intent.hasExtra(QUERY_EXTRA)) {
			query = intent.getParcelableExtra(QUERY_EXTRA);
			nameText.setText(query.getName());
			okButton.requestFocus();
		} else {
			query = new Query();
			nameText.requestFocus();
		}

		if (savedInstanceState != null) {
			// restore the edited assignment id
			editAssignmentId = savedInstanceState.getInt(
					EDIT_ASSIGNMENT_ID_STATE_KEY, -1);
		}

		// init context manager
		InitializationManager.initialize(this,
				contextInitializationFinishedListener, ContextProxyManager
						.getInstance(), ContextSearchManager.getInstance());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ContextSearchManager.getInstance().setOnValueSelectedListener(null);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Builder builder = new Builder(this);
		switch (id) {
			case DIALOG_CLEAR_ASSIGNMENTS_CONFIRM:
				builder
						.setMessage(
								R.string.query_editor_dialog_clear_assignments_confirm_message)
						.setCancelable(false).setPositiveButton(R.string.yes,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										clearAssignments();
									}
								}).setNegativeButton(R.string.no,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.cancel();
									}

								});
				return builder.create();
			case DIALOG_REPLACE_ASSIGNMENT:
				builder.setCancelable(false).setMessage("").setPositiveButton(
						R.string.yes, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								replaceAssignment();
							}
						}).setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
							}
						});
				return builder.create();
			default:
				return super.onCreateDialog(id);
		}
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {
			case DIALOG_REPLACE_ASSIGNMENT:
				AlertDialog alert = (AlertDialog) dialog;
				alert
						.setMessage(Html
								.fromHtml(getString(
										R.string.query_editor_dialog_replace_assignment_message,
										pendingValue.getName(),
										conflictingValue.getName(),
										conflictingRootDimension.getName())));
				break;
		}
	}

	private void createUI() {
		setContentView(R.layout.query_editor_activity);
		nameText = (EditText) findViewById(R.id.name);
		registerForContextMenu(getListView());
		okButton = (Button) findViewById(R.id.ok);
		okButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				save();
			}
		});
	}

	@Override
	public boolean onSearchRequested() {
		Bundle appData = new Bundle();
		appData.putStringArray(
				AssignmentSearchActivity.RESTRICTED_VALUE_URIS_BUNDLE_KEY,
				buildRestrictedValueURIs());
		startSearch(null, false, appData, false);
		return true;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		createUI();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// save the edited assignment id
		outState.putInt(EDIT_ASSIGNMENT_ID_STATE_KEY, editAssignmentId);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		editAssignment(position);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
			case R.id.delete:
				deleteAssignment((int) info.id);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.query_editor_context, menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (super.onCreateOptionsMenu(menu)) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.query_editor_options, menu);
			return true;
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (!super.onOptionsItemSelected(item)) {
			switch (item.getItemId()) {
				case R.id.navigate:
					addAssignmentByNavigation();
					return true;
				case R.id.search:
					addAssignmentBySearch();
					return true;
				case R.id.clear:
					showDialog(DIALOG_CLEAR_ASSIGNMENTS_CONFIRM);
					return true;
			}
			return false;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ADD_ASSIGNMENT_BY_NAVIGATION_REQUEST) {
			if (resultCode == Activity.RESULT_OK) {
				LightweightAssignment assignment = data
						.getParcelableExtra(AssignmentNavigationActivity.ASSIGNMENT_EXTRA);
				assignmentAdded(assignment);
			}
		} else if (requestCode == EDIT_ASSIGNMENT_REQUEST) {
			if (resultCode == Activity.RESULT_OK) {
				LightweightAssignment assignment = data
						.getParcelableExtra(AssignmentNavigationActivity.ASSIGNMENT_EXTRA);
				assignmentUpdated(assignment);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void initializeAssignmentAdapter() {
		assignmentAdapter = new AssignmentAdapter(QueryEditorActivity.this,
				query.getAssignments());
		setListAdapter(assignmentAdapter);
	}

	private void addAssignmentByNavigation() {
		editAssignmentId = -1;
		Intent intent = new Intent(this, AssignmentNavigationActivity.class);
		intent
				.putExtra(
						AssignmentNavigationActivity.RESTRICTED_ROOT_DIMENSIONS_URIS_EXTRA,
						buildRestrictedRootDimensionsURIs());
		startActivityForResult(intent, ADD_ASSIGNMENT_BY_NAVIGATION_REQUEST);
	}

	private void addAssignmentBySearch() {
		// request a search
		onSearchRequested();
	}

	private void checkSelectedValue(Value value) {
		// check if this value shares its root dimension with some assignment
		conflictingAssignmentId = -1;
		conflictingValue = null;
		Dimension rootDimension = value.getRootDimension();
		for (int i = 0; i < query.getAssignments().size(); i++) {
			LightweightAssignment assignment = query.getAssignments().get(i);
			Value assignmentValue = ContextProxyManager.getInstance()
					.getProxy().findValue(assignment.getValueURI());
			if (assignmentValue.getRootDimension().equals(rootDimension)) {
				conflictingAssignmentId = i;
				conflictingValue = assignmentValue;
				pendingValue = value;
				conflictingRootDimension = rootDimension;
				showDialog(DIALOG_REPLACE_ASSIGNMENT);
				return;
			}
		}

		LightweightAssignment assignment = new LightweightAssignment(value.getURI());
		assignmentAdded(assignment);
	}

	private void replaceAssignment() {
		LightweightAssignment assignment = query.getAssignments().get(
				conflictingAssignmentId);
		assignment.setValueURI(pendingValue.getURI());
		assignmentAdapter.notifyDataSetChanged();
		conflictingAssignmentId = -1;
		pendingValue = null;
		conflictingValue = null;
		conflictingRootDimension = null;
	}

	private void assignmentAdded(LightweightAssignment assignment) {
		query.getAssignments().add(assignment);
		assignmentAdapter.notifyDataSetChanged();
	}

	private void assignmentUpdated(LightweightAssignment assignment) {
		if (editAssignmentId != -1) {
			query.getAssignments().remove(editAssignmentId);
			query.getAssignments().add(editAssignmentId, assignment);
			assignmentAdapter.notifyDataSetChanged();
			editAssignmentId = -1;
		}
	}

	private void deleteAssignment(int id) {
		query.getAssignments().remove(id);
		assignmentAdapter.notifyDataSetChanged();
	}

	private void clearAssignments() {
		query.getAssignments().clear();
		assignmentAdapter.notifyDataSetChanged();
	}

	private void editAssignment(int id) {
		editAssignmentId = id;
		LightweightAssignment assignment = query.getAssignments().get(id);
		Intent intent = new Intent(this, AssignmentNavigationActivity.class);
		intent.putExtra(AssignmentNavigationActivity.ASSIGNMENT_EXTRA,
				(Parcelable) assignment);
		intent
				.putExtra(
						AssignmentNavigationActivity.RESTRICTED_ROOT_DIMENSIONS_URIS_EXTRA,
						buildRestrictedRootDimensionsURIs());
		startActivityForResult(intent, EDIT_ASSIGNMENT_REQUEST);
	}

	private String[] buildRestrictedRootDimensionsURIs() {
		int count = query.getAssignments().size();
		if (editAssignmentId != -1)
			count--;
		String[] uris = new String[count];
		int index = 0;
		for (int i = 0; i < query.getAssignments().size(); i++) {
			if (i != editAssignmentId) {
				LightweightAssignment assignment = query.getAssignments().get(i);
				Value value = ContextProxyManager.getInstance().getProxy()
						.findValue(assignment.getValueURI());
				uris[index++] = value.getRootDimension().getURI();
			}
		}

		return uris;
	}

	private String[] buildRestrictedValueURIs() {
		int count = query.getAssignments().size();
		String[] uris = new String[count];
		for (int i = 0; i < count; i++) {
			LightweightAssignment assignment = query.getAssignments().get(i);
			uris[i] = assignment.getValueURI();
		}

		return uris;
	}

	private void save() {
		if (query.getAssignments().isEmpty()) {
			Toast.makeText(this,
					this.getText(R.string.query_editor_toast_no_assignments),
					Toast.LENGTH_SHORT).show();
			return;
		}
		String name = nameText.getText().toString();
		if (TextUtils.isEmpty(name)) {
			Toast.makeText(this,
					this.getText(R.string.query_editor_toast_empty_name),
					Toast.LENGTH_SHORT).show();
			nameText.requestFocus();
			return;
		}
		query.setName(name);

		Intent intent = new Intent();
		intent.putExtra(QUERY_EXTRA, (Parcelable) query);
		setResult(Activity.RESULT_OK, intent);
		finish();
	}
}
