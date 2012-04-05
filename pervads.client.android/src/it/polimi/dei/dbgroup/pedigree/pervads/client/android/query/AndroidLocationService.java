package it.polimi.dei.dbgroup.pedigree.pervads.client.android.query;

import it.polimi.dei.dbgroup.pedigree.pervads.model.matching.LocationService;
import it.polimi.dei.dbgroup.pedigree.pervads.model.matching.specialized.Coordinates;

import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

public class AndroidLocationService implements LocationService {
	private Geocoder geocoder;
	private Context context;
	
	public AndroidLocationService(Context context) {
		this.context = context;
		this.geocoder = new Geocoder(context);
	}
	
	@Override
	public Coordinates geocode(String location) throws Exception {
		List<Address> addresses = geocoder.getFromLocationName(location, 1);
		if(addresses.size() == 0) return null;
		Address address = addresses.get(0);
		return new Coordinates(address.getLatitude(), address.getLongitude());
	}

	
	private static final String[] PROVIDERS = new String[] {
		LocationManager.GPS_PROVIDER,
		LocationManager.NETWORK_PROVIDER
	};
	
	@Override
	public Coordinates getCurrentPosition() throws Exception {
		LocationManager manager = getLocationManager();
		Location location = null;
		for(String provider : PROVIDERS) {
			location = manager.getLastKnownLocation(provider);
			if(location != null) break;
		}
		if(location == null) return null;
		return new Coordinates(location.getLatitude(), location.getLongitude());
	}

	private LocationManager getLocationManager() {
		return (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	}
}
