package it.polimi.dei.dbgroup.pedigree.pervads.client.android;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Dimension;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.NamedEntity;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.R;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.adapters.NamedEntityAdapter;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.context.ContextProxyManager;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.query.AssignmentProxy;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.widget.AssignmentView;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.widget.AssignmentView.OnEntityClickListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class AssignmentNavigationActivity extends ListActivity {
	public static final String ASSIGNMENT_EXTRA = "it.polimi.dei.dbgroup.pedigree.pervads.AssignmentNavigationActivity.Assignment";
	public static final String RESTRICTED_ROOT_DIMENSIONS_URIS_EXTRA = "it.polimi.dei.dbgroup.pedigree.pervads.AssignmentNavigationActivity.RestrictedRootDimensionsURIs";
	private AssignmentView assignmentView;
	private NamedEntityAdapter entitiesAdapter;
	private TextView chooseTextView;
	private ViewGroup buttons;
	private Value selectedValue = null;
	private String[] restrictedRootDimensionsURIs = new String[0];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.assignment_navigation_activity);

		assignmentView = (AssignmentView) findViewById(R.id.assignment);
		assignmentView.setOnEntityClickListener(new OnEntityClickListener() {

			@Override
			public void onValueClick(Value value) {
				setDimension(value.getParentDimension());
			}

			@Override
			public void onDimensionClick(Dimension dimension) {
				setValue(dimension.getParentValue());
			}
		});

		entitiesAdapter = new NamedEntityAdapter(this);
		setListAdapter(entitiesAdapter);

		chooseTextView = (TextView) findViewById(R.id.choose);

		buttons = (ViewGroup) findViewById(R.id.buttons);

		final Button okButton = (Button) findViewById(R.id.ok);
		okButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				returnResult();
			}
		});

		handleIntent();
	}

	private void handleIntent() {
		Intent intent = getIntent();
		if (intent.hasExtra(RESTRICTED_ROOT_DIMENSIONS_URIS_EXTRA)) {
			restrictedRootDimensionsURIs = intent
					.getStringArrayExtra(RESTRICTED_ROOT_DIMENSIONS_URIS_EXTRA);
			Arrays.sort(restrictedRootDimensionsURIs);
		}

		if (intent.hasExtra(ASSIGNMENT_EXTRA)) {
			// edit the assignment
			AssignmentProxy assignment = intent.getParcelableExtra(ASSIGNMENT_EXTRA);
			Value value = ContextProxyManager.getInstance().getProxy()
					.findValue(assignment.getValueURI());
			setValue(value);
		} else {
			// create a new assignment
			setValue(null);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		NamedEntity entity = entitiesAdapter.getEntity(position);
		if (entity instanceof Dimension)
			setDimension((Dimension) entity);
		else if (entity instanceof Value)
			setValue((Value) entity);
	}

	private void setValue(Value value) {
		this.selectedValue = value;
		if (value == null) {
			// hide assignment view
			assignmentView.setVisibility(View.GONE);

			Collection<? extends Dimension> rootDimensions = ContextProxyManager
					.getInstance().getProxy().listChildDimensions();

			// create the root dimensions list removing restricted dimensions
			List<Dimension> allowedDimensions = new ArrayList<Dimension>();
			for (Dimension dimension : rootDimensions) {
				if (Arrays.binarySearch(restrictedRootDimensionsURIs, dimension
						.getURI()) < 0)
					allowedDimensions.add(dimension);
			}

			// set the choose first dimension text
			if (allowedDimensions.isEmpty())
				chooseTextView
						.setText(R.string.assignment_navigation_choose_no_root_dimensions_text);
			else
				chooseTextView
						.setText(R.string.assignment_navigation_choose_first_dimension_text);

			// initialize the listview with root dimensions
			entitiesAdapter.setEntities(allowedDimensions);

			// hide the button panel
			buttons.setVisibility(View.GONE);
		} else {
			// update and show the assignment view
			assignmentView.setEntity(value);
			assignmentView.setVisibility(View.VISIBLE);

			Collection<? extends Dimension> valueSubDimensions = value
					.listChildDimensions();

			// set the choose sub dimension or ok text
			if (valueSubDimensions.isEmpty())
				chooseTextView
						.setText(R.string.assignment_navigation_choose_ok_or_back_text);
			else
				chooseTextView
						.setText(R.string.assignment_navigation_choose_sub_dimension_text);

			// initialize the listview with value subdimensions
			entitiesAdapter.setEntitiesFromCollection(valueSubDimensions);

			// show the button panel
			buttons.setVisibility(View.VISIBLE);
		}
	}

	private void setDimension(Dimension dimension) {
		this.selectedValue = null;

		// update and show the assignment view
		assignmentView.setEntity(dimension);
		assignmentView.setVisibility(View.VISIBLE);

		// set the choose value text
		chooseTextView
				.setText(R.string.assignment_navigation_choose_value_text);

		// initialize the listview with dimension values
		entitiesAdapter.setEntitiesFromCollection(dimension.listChildValues());

		// hide the button panel
		buttons.setVisibility(View.GONE);
	}

	private void returnResult() {
		if (selectedValue != null) {
			AssignmentProxy assignment = new AssignmentProxy(selectedValue.getURI());
			Intent intent = new Intent();
			intent.putExtra(ASSIGNMENT_EXTRA, (Parcelable) assignment);
			setResult(Activity.RESULT_OK, intent);
		} else {
			setResult(Activity.RESULT_CANCELED);
		}
		finish();
	}
}
