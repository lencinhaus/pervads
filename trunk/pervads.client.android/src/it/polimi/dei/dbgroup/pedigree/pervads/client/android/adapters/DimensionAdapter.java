package it.polimi.dei.dbgroup.pedigree.pervads.client.android.adapters;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.NamedEntity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DimensionAdapter extends NamedEntityAdapter {
	public DimensionAdapter(Context context) {
		super(context);
	}
	
	private static final class ViewHolder {
		public TextView name;
		public TextView description;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		NamedEntity entity = entities.get(position);
		if (convertView == null) {
			convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);

			holder = new ViewHolder();
			holder.text = (TextView) convertView.findViewById(android.R.id.text1);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.text.setText(entity.getName());

		return convertView;
	}

}
