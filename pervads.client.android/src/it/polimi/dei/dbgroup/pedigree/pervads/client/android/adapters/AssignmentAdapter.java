package it.polimi.dei.dbgroup.pedigree.pervads.client.android.adapters;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.R;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.R.id;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.R.layout;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.context.ContextProxyManager;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.query.AssignmentProxy;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AssignmentAdapter extends BaseAdapter {
	private LayoutInflater inflater = null;
	private List<AssignmentContainer> flatAssignments = new ArrayList<AssignmentContainer>();

	private static final class AssignmentContainer {
		public AssignmentProxy assignment;
		public int depth;

		public AssignmentContainer(AssignmentProxy assignment, int depth) {
			super();
			this.assignment = assignment;
			this.depth = depth;
		}
	}

	private class ViewHolder {
		public TextView value;
		public TextView dimension;
		public LinearLayout spacer;
	}

	public AssignmentAdapter(Context context) {
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setAssignments(List<AssignmentProxy> assignments) {
		flattenAssignments(assignments);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return flatAssignments.size();
	}

	@Override
	public Object getItem(int position) {
		return flatAssignments.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		AssignmentContainer container = flatAssignments.get(position);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.assignment_list_item,
					null);

			holder = new ViewHolder();
			holder.value = (TextView) convertView
					.findViewById(R.id.value);
			holder.dimension = (TextView) convertView
					.findViewById(R.id.dimension);
			holder.spacer = (LinearLayout) convertView.findViewById(R.id.spacer);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Value value = ContextProxyManager.getInstance().getProxy().findValue(container.assignment.getValueURI());
		holder.value.setText(value.getName());
		holder.dimension.setText(value.getParentDimension().getName());
		holder.spacer.setMinimumWidth(container.depth * 50);
		return convertView;
	}

	private void flattenAssignments(List<AssignmentProxy> assignments) {
		flatAssignments.clear();
		flattenAssignments(assignments, 0);
	}

	private void flattenAssignments(List<AssignmentProxy> assignments, int depth) {
		for (AssignmentProxy assignment : assignments) {
			flatAssignments.add(new AssignmentContainer(assignment, depth));
			flattenAssignments(assignment.getChildAssignments(), depth + 1);
		}
	}
}
