package it.polimi.dei.dbgroup.pedigree.pervads.client.android.adapters;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.NamedEntity;

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

public abstract class NamedEntityAdapter extends BaseAdapter {
	private static final Comparator<NamedEntity> NAMED_ENTITY_COMPARATOR = new Comparator<NamedEntity>() {
		
		@Override
		public int compare(NamedEntity object1, NamedEntity object2) {
			if(object1 == null) {
				if(object2 == null) return 0;
				else return -1;
			}
			else if(object2 == null) return 1;
			else return object1.getName().compareTo(object2.getName());
		}
	};

	protected List<? extends NamedEntity> entities = null;
	protected LayoutInflater inflater;
	
	public NamedEntityAdapter(Context context) {
		this.inflater = (LayoutInflater) context
		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	@Override
	public int getCount() {
		return (entities == null)?0:entities.size();
	}

	@Override
	public Object getItem(int position) {
		return getEntity(position);
	}
	
	public NamedEntity getEntity(int position) {
		return entities.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);
	
//	@Override
//	public View getView(int position, View convertView, ViewGroup parent) {
//		ViewHolder holder;
//		NamedEntity entity = entities.get(position);
//		if (convertView == null) {
//			convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
//
//			holder = new ViewHolder();
//			holder.text = (TextView) convertView.findViewById(android.R.id.text1);
//			convertView.setTag(holder);
//		} else {
//			holder = (ViewHolder) convertView.getTag();
//		}
//
//		holder.text.setText(entity.getName());
//
//		return convertView;
//	}

	public void setEntities(List<? extends NamedEntity> entities) {
		this.entities = entities;
		Collections.sort(this.entities, NAMED_ENTITY_COMPARATOR);
		notifyDataSetChanged();
	}
	
	public void setEntitiesFromCollection(Collection<? extends NamedEntity> collection) {
		List<NamedEntity> list = new ArrayList<NamedEntity>(collection);
		setEntities(list);
	}
}
