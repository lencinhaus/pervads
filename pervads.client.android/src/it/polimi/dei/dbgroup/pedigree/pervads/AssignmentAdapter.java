package it.polimi.dei.dbgroup.pedigree.pervads;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;
import it.polimi.dei.dbgroup.pedigree.pervads.context.ContextProxyManager;
import it.polimi.dei.dbgroup.pedigree.pervads.query.Assignment;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AssignmentAdapter extends BaseAdapter {
	private LayoutInflater inflater = null;
	private List<Assignment> assignments;

	private class ViewHolder {
		public TextView text;
		public TextView summary;
	}

	public AssignmentAdapter(Context context, List<Assignment> assignments) {
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.assignments = assignments;
	}

	@Override
	public int getCount() {
		return assignments.size();
	}

	@Override
	public Object getItem(int position) {
		return assignments.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		Assignment assignment = assignments.get(position);
		if (convertView == null) {
			convertView = inflater.inflate(android.R.layout.simple_list_item_2,
					null);

			holder = new ViewHolder();
			holder.text = (TextView) convertView
					.findViewById(android.R.id.text1);
			holder.summary = (TextView) convertView.findViewById(android.R.id.text2);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Value value = ContextProxyManager.getInstance().getProxy().findValue(
				assignment.getValueURI());
		holder.text.setText(value.getName());
		holder.summary.setText(value.getParentDimension().getName());
		return convertView;
	}

}
