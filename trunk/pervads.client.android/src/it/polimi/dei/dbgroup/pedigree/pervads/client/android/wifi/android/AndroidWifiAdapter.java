package it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi.android;

import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Logger;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Utils;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi.AbstractWifiAdapter;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi.ConnectionInfo;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi.IWifiAdapterListener;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi.WifiAdapterException;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi.WifiNetwork;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi.WifiSecurity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager.WifiLock;
import android.text.TextUtils;

public class AndroidWifiAdapter extends AbstractWifiAdapter {
	private Context context;
	private WifiManager manager;
	private boolean sessionStarted = false;
	private WifiLock lock = null;
	private int previouslyConnectedNetworkId = -1;
	private List<Integer> temporaryNetworkIds = new ArrayList<Integer>();
	private AdapterState adapterState = AdapterState.IDLE;
	private static final String LOCK_TAG = "pervads_wifi_lock";
	private static final Map<String, WifiSecurity> CAPABILITIES_SECURITY_MAPPINGS = new HashMap<String, WifiSecurity>();
	static {
		// we remove EAP because it will never be supported
		// CAPABILITIES_SECURITY_MAPPINGS.put("EAP", WifiSecurity.EAP);
		CAPABILITIES_SECURITY_MAPPINGS.put("PSK", WifiSecurity.WPA_PSK);
		CAPABILITIES_SECURITY_MAPPINGS.put("WEP", WifiSecurity.WEP);
	}
	private static final Logger L = new Logger(AndroidWifiAdapter.class
			.getSimpleName());

	private final BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
					&& adapterState == AdapterState.SCANNING) {
				// scan terminated, results available
				adapterState = AdapterState.IDLE;
				getListener().scanCompleted();
			} else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
				NetworkInfo info = (NetworkInfo) intent
						.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				String BSSID = intent.getStringExtra(WifiManager.EXTRA_BSSID);
				handleNetworkStateChange(info, BSSID);
			}
		}
	};

	private static final IntentFilter filter = new IntentFilter();

	static {
		filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
	}

	private enum AdapterState {
		IDLE, CONNECTING, DISCONNECTING, SCANNING
	}

	public AndroidWifiAdapter(IWifiAdapterListener listener, Context context) {
		super(listener);
		if (context == null)
			throw new NullPointerException("context cannot be null");
		this.context = context;
	}

	@Override
	public void initialize() {
		manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}

	@Override
	public boolean isSupported() {
		// try to open wifimanager service
		if (manager != null) {
			return manager.pingSupplicant();
		}
		return false;
	}

	@Override
	public boolean isEnabled() {
		if (!isSupported())
			return false;
		return manager.isWifiEnabled();
	}

	@Override
	public void connect(WifiNetwork network) throws WifiAdapterException {
		// if connected, try to disconnect
		if (isConnected()) {
			disconnect();
		}

		// get this network configuration
		WifiConfiguration configuration = getConfiguration(network);
		if (configuration == null)
			throw new WifiAdapterException(
					"cannot parse WifiConfiguration for network "
							+ network.getSSID());

		// enable the network
		adapterState = AdapterState.CONNECTING;
		if (!manager.enableNetwork(configuration.networkId, true)) {
			adapterState = AdapterState.IDLE;
			throw new WifiAdapterException("cannot enable network "
					+ network.getSSID());
		}
	}

	@Override
	public void disconnect() throws WifiAdapterException {
		if (!isConnected()) {
			return;
		}

		// disconnect from network
		adapterState = AdapterState.DISCONNECTING;
		if (!manager.disconnect()) {
			adapterState = AdapterState.IDLE;
			throw new WifiAdapterException(
					"cannot disconnect from current network");
		}
	}

	@Override
	public WifiNetwork[] getReachableNetworks() {
		// TODO maybe parse scan results when scan finishes?
		List<ScanResult> scanResults = manager.getScanResults();
		if (scanResults == null)
			return null;

		List<WifiNetwork> reachableNetworks = new ArrayList<WifiNetwork>();
		for (ScanResult scanResult : scanResults) {
			WifiNetwork network = createFromScanResult(scanResult);
			if (network != null)
				reachableNetworks.add(network);
		}
		return reachableNetworks.toArray(new WifiNetwork[reachableNetworks
				.size()]);
	}

	@Override
	public WifiNetwork getConnectedNetwork() {
		// get wifi info from manager
		WifiInfo wifiInfo = manager.getConnectionInfo();
		if (wifiInfo == null)
			return null;

		// check supplicant state
		if (wifiInfo.getSupplicantState() != SupplicantState.COMPLETED)
			return null;

		WifiConfiguration wifiConfiguration = findConfiguration(wifiInfo
				.getNetworkId());
		if (wifiConfiguration == null)
			return null;

		return createFromWifiInfoAndConfiguration(wifiInfo, wifiConfiguration);
	}

	@Override
	public ConnectionInfo getConnectionInfo() {
		// get wifi info from manager
		WifiInfo wifiInfo = manager.getConnectionInfo();
		if (wifiInfo == null)
			return null;

		// get dhcp info from manager
		DhcpInfo dhcpInfo = manager.getDhcpInfo();

		ConnectionInfo info = new ConnectionInfo();
		info.setIpAddress(wifiInfo.getIpAddress());
		info.setLinkSpeed(wifiInfo.getLinkSpeed());
		info.setMacAddress(wifiInfo.getMacAddress());

		if (dhcpInfo != null) {
			info.setDns1(dhcpInfo.dns1);
			info.setDns2(dhcpInfo.dns2);
			info.setGateway(dhcpInfo.gateway);
			info.setIpAddress(dhcpInfo.ipAddress);
			info.setLeaseDuration(dhcpInfo.leaseDuration);
			info.setNetmask(dhcpInfo.netmask);
			info.setServerAddress(dhcpInfo.serverAddress);
		}

		return info;
	}

	@Override
	public boolean isConnected() {
		WifiInfo info = manager.getConnectionInfo();
		if (info != null) {
			return info.getSupplicantState() == SupplicantState.COMPLETED;
		}
		return false;
	}

	@Override
	public void startNetworkScan() throws WifiAdapterException {
		adapterState = AdapterState.SCANNING;
		if (!manager.startScan()) {
			adapterState = AdapterState.IDLE;
			throw new WifiAdapterException("cannot start scan with WifiManager");
		}
	}

	@Override
	public void endSession() throws WifiAdapterException {
		synchronized (this) {
			if (!sessionStarted)
				throw new WifiAdapterException("no session was started");

			// remove temporary networks
			if (temporaryNetworkIds.size() > 0) {
				for (int networkId : temporaryNetworkIds) {
					manager.removeNetwork(networkId);
				}
				manager.saveConfiguration();
				temporaryNetworkIds.clear();
			}

			// release wifi lock
			lock.release();
			lock = null;

			// unregister broadcast receiver
			context.unregisterReceiver(receiver);
			
			// reconnect to previous network
			if(previouslyConnectedNetworkId != -1) {
				manager.enableNetwork(previouslyConnectedNetworkId, true);
			}

			sessionStarted = false;
		}
	}

	@Override
	public void startSession() throws WifiAdapterException {
		synchronized (this) {
			if (sessionStarted)
				throw new WifiAdapterException(
						"a session has already been started");
			
			// save current network id
			previouslyConnectedNetworkId = -1;
			WifiInfo info = manager.getConnectionInfo();
			if(info != null) {
				previouslyConnectedNetworkId = info.getNetworkId();
			}

			// register broadcast receiver
			context.registerReceiver(receiver, filter);

			// create and acquire lock
			lock = manager.createWifiLock(LOCK_TAG);
			lock.setReferenceCounted(false);
			lock.acquire();

			// clear temporary network ids list
			temporaryNetworkIds.clear();

			sessionStarted = true;
		}
	}

	private static WifiNetwork createFromScanResult(ScanResult scanResult) {
		WifiNetwork network = new WifiNetwork();
		network.setSSID(scanResult.SSID);
		network.setBSSID(scanResult.BSSID);
		network
				.setSecurity(parseSecurityFromScanResultCapabilities(scanResult.capabilities));
		network.setSignalLevel(scanResult.level);
		return network;
	}

	private static WifiSecurity parseSecurityFromScanResultCapabilities(
			String capabilities) {
		for (String securityCapability : CAPABILITIES_SECURITY_MAPPINGS
				.keySet()) {
			if (capabilities.contains(securityCapability))
				return CAPABILITIES_SECURITY_MAPPINGS.get(securityCapability);
		}
		return WifiSecurity.OPEN;
	}

	private static WifiNetwork createFromWifiInfoAndConfiguration(
			WifiInfo wifiInfo, WifiConfiguration wifiConfiguration) {
		WifiNetwork network = new WifiNetwork();
		network.setSSID(wifiConfiguration.SSID);
		network.setBSSID(wifiConfiguration.BSSID);
		network.setSignalLevel(wifiInfo.getRssi());
		network.setSecurity(getWifiConfigurationSecurity(wifiConfiguration));
		return network;
	}

	private static WifiSecurity getWifiConfigurationSecurity(
			WifiConfiguration wifiConfig) {
		// TODO handle EAP networks
		if (!TextUtils.isEmpty(wifiConfig.preSharedKey)) {
			return WifiSecurity.WPA_PSK;
		} else if (!TextUtils.isEmpty(wifiConfig.wepKeys[0])) {
			return WifiSecurity.WEP;
		}
		return WifiSecurity.OPEN;
	}

	private WifiConfiguration findConfiguration(int networkId) {
		for (WifiConfiguration configuredNetwork : manager
				.getConfiguredNetworks()) {
			if (configuredNetwork.networkId == networkId)
				return configuredNetwork;
		}

		return null;
	}

	private WifiConfiguration getConfiguration(WifiNetwork network) {
		// check if network is already configured. in this case, password is
		// ignored.
		 for (WifiConfiguration wifiConfiguration : manager
		 .getConfiguredNetworks()) {
		 if (matchConfiguration(wifiConfiguration, network))
		 return wifiConfiguration;
		 }

		// network is not configured, we have to create a temporary
		// configuration
		WifiConfiguration wifiConfiguration = new WifiConfiguration();
		wifiConfiguration.BSSID = network.getBSSID();
		wifiConfiguration.priority = 0; // TODO is it ok?
		wifiConfiguration.SSID = Utils.convertToQuotedString(network.getSSID());
		wifiConfiguration.hiddenSSID = false;

		// setup security

		wifiConfiguration.allowedAuthAlgorithms.clear();
		wifiConfiguration.allowedGroupCiphers.clear();
		wifiConfiguration.allowedKeyManagement.clear();
		wifiConfiguration.allowedPairwiseCiphers.clear();
		wifiConfiguration.allowedProtocols.clear();

		String p = network.getPassword();
		switch (network.getSecurity()) {
			case WEP:
			case WEP_40:
			case WEP_104:
			case WEP_232:
			case WEP_SHARED:
			case WEP_SHARED_40:
			case WEP_SHARED_104:
			case WEP_SHARED_232:
				if (!TextUtils.isEmpty(p)) {
					if (isHexWepKey(p, network.getSecurity()))
						wifiConfiguration.wepKeys[0] = p;
					else
						wifiConfiguration.wepKeys[0] = Utils
								.convertToQuotedString(p);
				}
				wifiConfiguration.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
				wifiConfiguration.allowedAuthAlgorithms
						.set(AuthAlgorithm.SHARED);
				wifiConfiguration.allowedKeyManagement.set(KeyMgmt.NONE);
				wifiConfiguration.wepTxKeyIndex = 0;
				break;
			case WPA_PSK:
			case WPA2_PSK:
				if (!TextUtils.isEmpty(p)) {
					if (isHexWpaKey(p))
						wifiConfiguration.preSharedKey = p;
					else
						wifiConfiguration.preSharedKey = Utils
								.convertToQuotedString(p);
				}
				break;
			case OPEN:
				wifiConfiguration.allowedKeyManagement.set(KeyMgmt.NONE);
				break;
		}

		// we add the new configuration and save it
		// TODO better error handling is needed
		int networkId = manager.addNetwork(wifiConfiguration);
		if (networkId == -1)
			return null;
		wifiConfiguration.networkId = networkId;
		// if (!manager.saveConfiguration())
		// return null;

		return wifiConfiguration;
	}

	private void handleNetworkStateChange(NetworkInfo info, String BSSID) {
		if (Logger.V)
			L.v("handleNetworkStateChange: BSSID=" + BSSID + ", adapterState="
					+ adapterState.toString() + ", state="
					+ info.getState().toString() + ", detailedState="
					+ info.getDetailedState().toString());
		DetailedState state = info.getDetailedState();
		if (adapterState == AdapterState.CONNECTING) {
			if (state == DetailedState.DISCONNECTED || state == DetailedState.FAILED
					|| state == DetailedState.CONNECTED) {
				adapterState = AdapterState.IDLE;
				if (state == DetailedState.CONNECTED)
					getListener().connectionCompleted();
				else
					getListener().connectionFailed(null);
			}
		} else if (adapterState == AdapterState.DISCONNECTING) {
			if(state == DetailedState.FAILED || state == DetailedState.DISCONNECTED) {
				adapterState = AdapterState.IDLE;
				if(state == DetailedState.DISCONNECTED) 
					getListener().disconnectionCompleted();
				else
					getListener().disconnectionFailed(null);
			}
		}
//		if (info.getState() == State.CONNECTED
//				&& adapterState == AdapterState.CONNECTING) {
//			adapterState = AdapterState.IDLE;
//			getListener().connectionCompleted();
//		} else if (info.getState() == State.DISCONNECTED
//				&& adapterState == AdapterState.DISCONNECTING) {
//			adapterState = AdapterState.IDLE;
//			getListener().disconnectionCompleted();
//		}
	}

	private static boolean matchConfiguration(
			WifiConfiguration wifiConfiguration, WifiNetwork network) {
		String SSID = wifiConfiguration.SSID;

		// remove quotes from config SSID
		if (!TextUtils.isEmpty(SSID)) {
			if (Utils.isQuotedString(SSID)) {
				SSID = Utils.convertToUnquotedString(SSID);
			}
		}

		if (!network.getSSID().equals(SSID))
			return false;
		if (!network.getBSSID().equals(wifiConfiguration.BSSID))
			return false;
		if (network.getSecurity() != getWifiConfigurationSecurity(wifiConfiguration))
			return false;
		return true;
	}

	private static boolean isHexWepKey(String wepKey, WifiSecurity security) {
		final int len = wepKey.length();

		// WEP-40, WEP-104, and some vendors using 256-bit WEP (WEP-232?)
		if ((security == WifiSecurity.WEP_40 || security == WifiSecurity.WEP_SHARED_40)
				&& len != 10)
			return false;
		if ((security == WifiSecurity.WEP_104 || security == WifiSecurity.WEP_SHARED_104)
				&& len != 26)
			return false;
		if ((security == WifiSecurity.WEP_232 || security == WifiSecurity.WEP_SHARED_232)
				&& len != 58)
			return false;
		if ((security == WifiSecurity.WEP || security == WifiSecurity.WEP_SHARED)
				&& len != 10 && len != 26 && len != 58) {
			return false;
		}

		return Utils.isHexString(wepKey);
	}

	private static boolean isHexWpaKey(String wpaKey) {
		if (wpaKey.length() != 64)
			return false;

		return Utils.isHexString(wpaKey);
	}
}
