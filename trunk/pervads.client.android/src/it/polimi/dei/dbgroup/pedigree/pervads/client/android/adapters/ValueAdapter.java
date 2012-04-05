package it.polimi.dei.dbgroup.pedigree.pervads.client.android.adapters;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class ValueAdapter extends BaseAdapter {

	private LayoutInflater inflater = null;
	@SuppressWarnings("unchecked")
	private List<? extends Value> values = Collections.EMPTY_LIST;

	private class ViewHolder {
		public TextView text;
		public TextView summary;
	}

	public ValueAdapter(Context context) {
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setValues(List<? extends Value> values) {
		this.values = values;
		notifyDataSetChanged();
	}

	public Value getValue(int position) {
		return values.get(position);
	}

	@Override
	public int getCount() {
		return values.size();
	}

	@Override
	public Object getItem(int position) {
		return values.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		Value value = values.get(position);
		if (convertView == null) {
			convertView = inflater.inflate(android.R.layout.simple_list_item_2,
					null);

			holder = new ViewHolder();
			holder.text = (TextView) convertView
					.findViewById(android.R.id.text1);
			holder.summary = (TextView) convertView
					.findViewById(android.R.id.text2);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.text.setText(value.getName());
		holder.summary.setText(value.getParentDimension().getName());
		return convertView;
	}

}
