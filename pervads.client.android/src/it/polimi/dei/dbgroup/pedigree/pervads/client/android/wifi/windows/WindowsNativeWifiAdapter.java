package it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi.windows;

import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Logger;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi.AbstractWifiAdapter;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi.ConnectionInfo;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi.IWifiAdapterListener;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi.WifiAdapterException;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi.WifiNetwork;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi.WifiSecurity;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public final class WindowsNativeWifiAdapter extends AbstractWifiAdapter {
	private static final Logger L = new Logger(WindowsNativeWifiAdapter.class
			.getSimpleName());
	private static final String SERVICE_BASE_URI = "http://10.0.2.2:8000/wifiservice/";
	private static final String PING_METHOD = "ping";
	private static final String IS_ENABLED_METHOD = "enabled";
	private static final String IS_CONNECTED_METHOD = "connected";
	private static final String CONNECTION_INFO_METHOD = "connection";
	private static final String NETWORKS_METHOD = "networks";
	private static final String CONNECTED_NETWORK_METHOD = "network";
	private static final String CONNECT_METHOD = "connect";
	private static final String CONNECT_NETWORK_PARAM = "network";
	private static final String NETWORK_SSID_KEY = "SSID";
	private static final String NETWORK_BSSID_KEY = "BSSID";
	private static final String NETWORK_PASSWORD_KEY = "password";
	private static final String NETWORK_SIGNAL_LEVEL_KEY = "signalLevel";
	private static final String NETWORK_SECURITY_KEY = "security";
	private static final String NETWORK_PROFILE_KEY = "profile";
	private static final String NETWORK_INTERFACE_GUID_KEY = "interfaceGuid";
	private static final String CONNECTION_DNS1_KEY = "dns1";
	private static final String CONNECTION_DNS2_KEY = "dns2";
	private static final String CONNECTION_GATEWAY_KEY = "gateway";
	private static final String CONNECTION_IP_ADDRESS_KEY = "ipAddress";
	private static final String CONNECTION_LEASE_DURATION_KEY = "leaseDuration";
	private static final String CONNECTION_LINK_SPEED_KEY = "linkSpeed";
	private static final String CONNECTION_MAC_ADDRESS_KEY = "macAddress";
	private static final String CONNECTION_NETMASK_KEY = "netmask";
	private static final String CONNECTION_SERVER_ADDRESS_KEY = "serverAddress";
	private AspNetJsonServiceClient client = null;
	private WifiNetwork[] reachableNetworks = null;
	private boolean sessionStarted = false;

	private final Runnable SCAN_RUNNER = new Runnable() {

		@Override
		public void run() {
			try {
				updateReachableNetworks();

				getListener().scanCompleted();
			} catch (WifiAdapterException ex) {
				getListener().scanFailed(ex);
			}
		}
	};
	
	private class ConnectRunner implements Runnable
	{
		private WifiNetwork network;
		
		public void setNetwork(WifiNetwork network)
		{
			this.network = network;
		}
		
		@Override
		public void run() {
			if(network == null) throw new NullPointerException("network cannot be null");
			try
			{
				if(!callConnect(network)) getListener().connectionFailed(null);
				else getListener().connectionCompleted();
			}
			catch(WifiAdapterException ex)
			{
				getListener().connectionFailed(ex);
			}
		}
		
	}
	
	private final ConnectRunner CONNECT_RUNNER = new ConnectRunner();

	public WindowsNativeWifiAdapter(IWifiAdapterListener listener) {
		super(listener);
	}

	@Override
	public void initialize() throws WifiAdapterException {
		// create json client
		if(Logger.V) L.v("creating ASP.NET JSON service client with base URI " + SERVICE_BASE_URI);
		try {
			client = new AspNetJsonServiceClient(SERVICE_BASE_URI);
			if(Logger.D) L.d("ASP.NET json service client created");
		} catch (URISyntaxException ex) {
			throw new WifiAdapterException(
					"cannot create ASP.NET json service client with base URI "
							+ SERVICE_BASE_URI, ex);
		}

		// initialize empty reachable networks
		reachableNetworks = new WifiNetwork[0];
	}

	@Override
	public void connect(WifiNetwork network) throws WifiAdapterException {
		// start connection in new thread
		CONNECT_RUNNER.setNetwork(network);
		Thread t = new Thread(CONNECT_RUNNER);
		t.start();
	}

	@Override
	public void endSession() throws WifiAdapterException {
		sessionStarted = false;
	}

	@Override
	public WifiNetwork getConnectedNetwork() throws WifiAdapterException {
		return callGetConnectedNetwork();
	}

	@Override
	public ConnectionInfo getConnectionInfo() throws WifiAdapterException {
		return callGetConnectionInfo();
	}

	@Override
	public WifiNetwork[] getReachableNetworks() {
		return reachableNetworks;
	}

	@Override
	public boolean isConnected() throws WifiAdapterException {
		return callIsConnected();
	}

	@Override
	public boolean isEnabled() throws WifiAdapterException {
		return callIsEnabled();
	}

	@Override
	public boolean isSupported() throws WifiAdapterException {
		return callPing();
	}

	@Override
	public void startNetworkScan() {
		// start scan in a new thread
		Thread t = new Thread(SCAN_RUNNER);
		t.start();
	}

	@Override
	public void startSession() {
		sessionStarted = true;
	}
	
	

	@Override
	public boolean isSessionStarted() throws WifiAdapterException {
		return sessionStarted;
	}

	private boolean callPing() throws WifiAdapterException {
		return client.makeBooleanRequest(PING_METHOD, null);
	}

	private boolean callIsEnabled() throws WifiAdapterException {
		return client.makeBooleanRequest(IS_ENABLED_METHOD, null);
	}

	private boolean callIsConnected() throws WifiAdapterException {
		return client.makeBooleanRequest(IS_CONNECTED_METHOD, null);
	}
	
	private boolean callConnect(WifiNetwork network) throws WifiAdapterException {
		JSONObject jsonNetwork = createJSONfromNetwork(network);
		JSONObject params = new JSONObject();
		try
		{
			params.put(CONNECT_NETWORK_PARAM, jsonNetwork);
		}
		catch(JSONException ex)
		{
			throw new WifiAdapterException("cannot create json connect parameters", ex);
		}
		
		return client.makeBooleanRequest(CONNECT_METHOD, params);
	}

	private ConnectionInfo callGetConnectionInfo() throws WifiAdapterException {
		JSONObject json = client
				.makeObjectRequest(CONNECTION_INFO_METHOD, null);
		return createConnectionInfoFromJSON(json);
	}
	
	private WifiNetwork callGetConnectedNetwork() throws WifiAdapterException {
		JSONObject json = client.makeObjectRequest(CONNECTED_NETWORK_METHOD, null);
		
		return createNetworkFromJSON(json);
	}

	private WifiNetwork[] callGetReachableNetworks()
			throws WifiAdapterException {
		final JSONArray jsonNetworks = client.makeArrayRequest(NETWORKS_METHOD,
				null);

		final List<WifiNetwork> networks = new ArrayList<WifiNetwork>();
		for (int i = 0; i < jsonNetworks.length(); i++) {
			if (!jsonNetworks.isNull(i)) {
				try {
					final JSONObject jsonNetwork = jsonNetworks
							.getJSONObject(i);

					networks.add(createNetworkFromJSON(jsonNetwork));
				} catch (Exception ex) {
					if(Logger.W) L.w("cannot parse json object", ex);
				}
			}
		}

		return networks.toArray(new WifiNetwork[networks.size()]);
	}

	private void updateReachableNetworks() throws WifiAdapterException {
		reachableNetworks = callGetReachableNetworks();
	}

	private static ConnectionInfo createConnectionInfoFromJSON(JSONObject json)
			throws WifiAdapterException {
		ConnectionInfo info = new ConnectionInfo();
		try {
			if (json.has(CONNECTION_DNS1_KEY)
					&& !json.isNull(CONNECTION_DNS1_KEY))
				info.setDns1(json.getInt(CONNECTION_DNS1_KEY));
			if (json.has(CONNECTION_DNS2_KEY)
					&& !json.isNull(CONNECTION_DNS2_KEY))
				info.setDns2(json.getInt(CONNECTION_DNS2_KEY));
			if (json.has(CONNECTION_GATEWAY_KEY)
					&& !json.isNull(CONNECTION_GATEWAY_KEY))
				info.setGateway(json.getInt(CONNECTION_GATEWAY_KEY));
			if (json.has(CONNECTION_IP_ADDRESS_KEY)
					&& !json.isNull(CONNECTION_IP_ADDRESS_KEY))
				info.setIpAddress(json.getInt(CONNECTION_IP_ADDRESS_KEY));
			if (json.has(CONNECTION_LEASE_DURATION_KEY)
					&& !json.isNull(CONNECTION_LEASE_DURATION_KEY))
				info.setLeaseDuration(json
						.getInt(CONNECTION_LEASE_DURATION_KEY));
			if (json.has(CONNECTION_LINK_SPEED_KEY)
					&& !json.isNull(CONNECTION_LINK_SPEED_KEY))
				info.setLinkSpeed(json.getInt(CONNECTION_LINK_SPEED_KEY));
			if (json.has(CONNECTION_MAC_ADDRESS_KEY)
					&& !json.isNull(CONNECTION_MAC_ADDRESS_KEY))
				info.setMacAddress(json.getString(CONNECTION_MAC_ADDRESS_KEY));
			if (json.has(CONNECTION_NETMASK_KEY)
					&& !json.isNull(CONNECTION_NETMASK_KEY))
				info.setNetmask(json.getInt(CONNECTION_NETMASK_KEY));
			if (json.has(CONNECTION_SERVER_ADDRESS_KEY)
					&& !json.isNull(CONNECTION_SERVER_ADDRESS_KEY))
				info.setServerAddress(json
						.getInt(CONNECTION_SERVER_ADDRESS_KEY));
		} catch (Exception jsex) {
			throw new WifiAdapterException(
					"cannot parse json connection info object", jsex);
		}

		return info;
	}

	private static WifiNetwork createNetworkFromJSON(JSONObject jsonNetwork)
			throws WifiAdapterException {
		final WifiNetwork network = new WifiNetwork();
		try {
			network.setSSID(jsonNetwork.getString(NETWORK_SSID_KEY));
			network.setBSSID(jsonNetwork.getString(NETWORK_BSSID_KEY));
			if (jsonNetwork.has(NETWORK_SIGNAL_LEVEL_KEY)
					&& !jsonNetwork.isNull(NETWORK_SIGNAL_LEVEL_KEY))
				network.setSignalLevel(jsonNetwork
						.getInt(NETWORK_SIGNAL_LEVEL_KEY));
			else
				network.setSignalLevel(WifiNetwork.SIGNAL_LEVEL_UNKNOWN);

			network.setSecurity(Enum.valueOf(WifiSecurity.class, jsonNetwork
					.getString(NETWORK_SECURITY_KEY)));

			if (jsonNetwork.has(NETWORK_PASSWORD_KEY)
					&& !jsonNetwork.isNull(NETWORK_PASSWORD_KEY)) {
				network
						.setPassword(jsonNetwork
								.getString(NETWORK_PASSWORD_KEY));
				if (TextUtils.isEmpty(network.getPassword()))
					network.setPassword(null);
			}

			// extended props
			if (jsonNetwork.has(NETWORK_PROFILE_KEY)
					&& !jsonNetwork.isNull(NETWORK_PROFILE_KEY))
				network.getExtendedProperties().put(NETWORK_PROFILE_KEY,
						jsonNetwork.getString(NETWORK_PROFILE_KEY));
			network.getExtendedProperties().put(NETWORK_INTERFACE_GUID_KEY,
					jsonNetwork.getString(NETWORK_INTERFACE_GUID_KEY));
		} catch (Exception ex) {
			if(Logger.W) L.w("cannot parse json network object", ex);
		}
		return network;
	}

	private static JSONObject createJSONfromNetwork(WifiNetwork network)
			throws WifiAdapterException {
		JSONObject jsonNetwork = new JSONObject();
		try {
			jsonNetwork.put(NETWORK_SSID_KEY, network.getSSID());

			jsonNetwork.put(NETWORK_BSSID_KEY, network.getBSSID());

			if (network.getSignalLevel() != WifiNetwork.SIGNAL_LEVEL_UNKNOWN)
				jsonNetwork.put(NETWORK_SIGNAL_LEVEL_KEY, network
						.getSignalLevel());
			else
				jsonNetwork.put(NETWORK_SIGNAL_LEVEL_KEY, JSONObject.NULL);

			jsonNetwork.put(NETWORK_SECURITY_KEY, network.getSecurity()
					.toString());

			if (TextUtils.isEmpty(network.getPassword()))
				jsonNetwork.put(NETWORK_PASSWORD_KEY, JSONObject.NULL);
			else
				jsonNetwork.put(NETWORK_PASSWORD_KEY, network.getPassword());

			// extended properties
			// interface guid is required
			jsonNetwork.put(NETWORK_INTERFACE_GUID_KEY, network
					.getExtendedProperties().get(NETWORK_INTERFACE_GUID_KEY));

			if (network.getExtendedProperties()
					.containsKey(NETWORK_PROFILE_KEY))
				jsonNetwork.put(NETWORK_PROFILE_KEY, network
						.getExtendedProperties().get(NETWORK_PROFILE_KEY));
			else
				jsonNetwork.put(NETWORK_PROFILE_KEY, JSONObject.NULL);
		} catch (Exception ex) {
			throw new WifiAdapterException("cannot create json network object",
					ex);
		}
		return jsonNetwork;
	}
}
