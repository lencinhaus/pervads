package it.polimi.dei.dbgroup.pedigree.pervads.client.android;

import it.polimi.dei.dbgroup.pedigree.pervads.client.android.query.QueryResult;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.query.ResultManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class QueryResultAdapter extends BaseAdapter {

	private LayoutInflater inflater = null;

	private ResultManager manager = null;

	private class ViewHolder {
		public TextView text1;
		public TextView text2;
	}

	public QueryResultAdapter(Context context) {
		super();
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.manager = new ResultManager(context);
	}

	@Override
	public int getCount() {
		return manager.getResults().size();
	}

	@Override
	public Object getItem(int position) {
		return manager.getResults().get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void update() {
		manager.synchronize();
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		QueryResult result = manager.getResults().get(position);
		if (convertView == null) {
			convertView = inflater.inflate(android.R.layout.simple_list_item_2,
					null);

			holder = new ViewHolder();
			holder.text1 = (TextView) convertView
					.findViewById(android.R.id.text1);
			holder.text2 = (TextView) convertView
					.findViewById(android.R.id.text2);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.text1.setText(result.getQueryName());
		holder.text2.setText(result.getMatchingPervADs().size() + " pervads");

		return convertView;
	}

}
