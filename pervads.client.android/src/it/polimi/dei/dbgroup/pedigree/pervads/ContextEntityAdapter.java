package it.polimi.dei.dbgroup.pedigree.pervads;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ContextEntityAdapter extends BaseAdapter {
	private static final Comparator<ContextEntity> NAMED_RESOURCE_COMPARATOR = new Comparator<ContextEntity>() {
		
		@Override
		public int compare(ContextEntity object1, ContextEntity object2) {
			if(object1 == null) {
				if(object2 == null) return 0;
				else return -1;
			}
			else if(object2 == null) return 1;
			else return object1.getName().compareTo(object2.getName());
		}
	};
	private class ViewHolder {
		public TextView text;
	}

	private List<? extends ContextEntity> entities = null;
	private LayoutInflater inflater;
	
	public ContextEntityAdapter(Context context) {
		this.inflater = (LayoutInflater) context
		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	@Override
	public int getCount() {
		return (entities == null)?0:entities.size();
	}

	@Override
	public Object getItem(int position) {
		return entities.get(position);
	}
	
	public ContextEntity getEntity(int position) {
		return entities.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		ContextEntity entity = entities.get(position);
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

	public void setEntities(List<? extends ContextEntity> resources) {
		this.entities = resources;
		Collections.sort(this.entities, NAMED_RESOURCE_COMPARATOR);
		notifyDataSetChanged();
	}
	
	public void setEntitiesFromCollection(Collection<? extends ContextEntity> collection) {
		List<ContextEntity> list = new ArrayList<ContextEntity>(collection);
		setEntities(list);
	}
}
