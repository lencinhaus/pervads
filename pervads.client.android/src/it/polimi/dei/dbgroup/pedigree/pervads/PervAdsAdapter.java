package it.polimi.dei.dbgroup.pedigree.pervads;

import java.util.Arrays;
import java.util.Comparator;

import it.polimi.dei.dbgroup.pedigree.pervads.util.Logger;
import it.polimi.dei.dbgroup.pedigree.pervads.IPervAdsService;
import it.polimi.dei.dbgroup.pedigree.pervads.R;
import android.content.Context;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PervAdsAdapter extends BaseAdapter {
	private static final Logger L = new Logger(PervAdsAdapter.class
			.getSimpleName());
	private LayoutInflater inflater = null;
	private IPervAdsService service = null;
	private PervAd[] pervAds = new PervAd[0];

	private static final Comparator<PervAd> COMPARATOR = new Comparator<PervAd>() {

		@Override
		public int compare(PervAd object1, PervAd object2) {
			boolean seen1 = object1.isSeen();
			boolean seen2 = object2.isSeen();
			if(!seen1 && seen2) return -1;
			if(seen1 && !seen2) return 1;
			long findTime1 = object1.getFindTime();
			long findTime2 = object2.getFindTime();
			return findTime2 > findTime1 ? 1 : (findTime2 < findTime1 ? -1 : 0);
		}
		
	};
	private class ViewHolder {
		public TextView text;
	}

	public PervAdsAdapter(Context context) {
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void update() {
		if (service != null) {
			try {
				pervAds = service.getPervAds();
				Arrays.sort(pervAds, COMPARATOR);
				notifyDataSetChanged();
			} catch (RemoteException ex) {
				// TODO notify exception
				if(Logger.W) L.w("cannot retrieve pervads for update", ex);
			}
		}
	}

	@Override
	public int getCount() {
		return pervAds.length;
	}

	@Override
	public Object getItem(int position) {
		return pervAds[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	

	@Override
	public int getItemViewType(int position) {
		return getPervAd(position).isSeen()?0:1;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		PervAd pervAd = getPervAd(position);
		if (convertView == null) {
			int layoutResourceID = pervAd.isSeen()?R.layout.pervads_list_item:R.layout.pervads_list_item_unseen;
			convertView = inflater.inflate(layoutResourceID, null);

			holder = new ViewHolder();
			holder.text = (TextView) convertView.findViewById(R.id.item_text);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.text.setText(pervAd.getId());

		return convertView;
	}

	public IPervAdsService getService() {
		return service;
	}

	public void setService(IPervAdsService service) {
		this.service = service;
	}
	
	public PervAd getPervAd(int position)
	{
		if(position < 0 || position >= pervAds.length) throw new IllegalArgumentException("position is not valid");
		return pervAds[position];
	}
}