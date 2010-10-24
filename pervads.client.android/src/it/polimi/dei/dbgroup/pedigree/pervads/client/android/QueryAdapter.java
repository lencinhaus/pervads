package it.polimi.dei.dbgroup.pedigree.pervads.client.android;

import it.polimi.dei.dbgroup.pedigree.pervads.client.android.query.Query;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

public class QueryAdapter extends BaseAdapter {
	private LayoutInflater inflater = null;

	private List<Query> queries;

	private class ViewHolder {
		public CheckedTextView checkedTextView;
	}

	public QueryAdapter(Context context, List<Query> queries) {
		super();
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.queries = queries;
	}

	@Override
	public int getCount() {
		return queries.size();
	}

	@Override
	public Object getItem(int position) {
		return queries.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		Query query = queries.get(position);
		if (convertView == null) {
			convertView = inflater.inflate(
					android.R.layout.simple_list_item_checked, null);

			holder = new ViewHolder();
			holder.checkedTextView = (CheckedTextView) convertView
					.findViewById(android.R.id.text1);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.checkedTextView.setText(query.getName());
		holder.checkedTextView.setChecked(query.isEnabled());

		return convertView;
	}
}
