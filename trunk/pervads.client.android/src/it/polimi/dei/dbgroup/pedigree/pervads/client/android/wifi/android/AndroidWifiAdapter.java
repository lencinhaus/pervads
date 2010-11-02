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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.GroupCipher;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.PairwiseCipher;
import android.net.wifi.WifiConfiguration.Protocol;
import android.net.wifi.WifiManager.WifiLock;
import android.text.TextUtils;

public class AndroidWifiAdapter extends AbstractWifiAdapter {
	private Context context;
	private WifiManager manager;
	private boolean sessionStarted = false;
	private WifiLock lock = null;
	private int previouslyConnectedNetworkId = -1;
	private List<Integer> temporaryNetworkIds = new ArrayList<Integer>();
	private WifiNetwork[] reachableNetworks = null;
	private AdapterState adapterState = AdapterState.IDLE;
	private WifiNetwork connectingTo = null;
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
				handleScanResultsAvailable();
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
		IDLE, CONNECTING, /* DISCONNECTING, */SCANNING
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
	public synchronized void connect(WifiNetwork network)
			throws WifiAdapterException {
		if (adapterState != AdapterState.IDLE)
			throw new WifiAdapterException(
					"Cannot connect while adapter is busy (" + adapterState
							+ ")");

		if (Logger.D)
			L.d("trying to connect to " + network);

		// if the currently connected network is the one we want to connect to,
		// just notify about successful connection
		WifiInfo info = manager.getConnectionInfo();
		if (info != null) {
			if (Logger.D)
				L.d("current connection: " + info
						+ "\nChecking if is the same as " + network);
			if (matchConnection(info, network)) {
				if (Logger.D)
					L.d("already connected to " + network);
				adapterState = AdapterState.IDLE;
				final Runnable notificationRunnable = new Runnable() {

					@Override
					public void run() {
						getListener().connectionCompleted();
					}
				};
				new Thread(notificationRunnable).start();

				return;
			} else if (Logger.D)
				L.d("current connection is not " + network
						+ ", will disconnect");
		} else {
			if (Logger.D)
				L.d("no current connection");
		}

		// get this network configuration
		WifiConfiguration configuration = getConfiguration(network);
		if (configuration == null)
			throw new WifiAdapterException(
					"cannot parse WifiConfiguration for network "
							+ network.getSSID());

		if (Logger.D)
			L.d("trying to connecto with configuration " + configuration);

		// enable the network
		connectingTo = network;
		adapterState = AdapterState.CONNECTING;
		if (!manager.enableNetwork(configuration.networkId, true)) {
			if (Logger.W)
				L.w("cannot enable configuration " + configuration);
			adapterState = AdapterState.IDLE;
			throw new WifiAdapterException("cannot enable network "
					+ network.getSSID());
		}
	}

	@Override
	public synchronized WifiNetwork[] getReachableNetworks() {
		return reachableNetworks;
	}

	@Override
	public WifiNetwork getConnectedNetwork() {
		// check if we are connected
		if (!isConnected())
			return null;

		// get wifi info from manager
		WifiInfo wifiInfo = manager.getConnectionInfo();
		if (wifiInfo == null)
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
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (manager != null) {
			NetworkInfo info = manager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (info != null && info.isConnected())
				return true;
		}
		return false;
	}

	@Override
	public synchronized void startNetworkScan() throws WifiAdapterException {
		adapterState = AdapterState.SCANNING;
		if (!manager.startScan()) {
			adapterState = AdapterState.IDLE;
			throw new WifiAdapterException("cannot start scan with WifiManager");
		}
	}

	@Override
	public synchronized void endSession() throws WifiAdapterException {
		if (!sessionStarted)
			throw new WifiAdapterException("no session was started");

		// remove temporary networks
		for (int networkId : temporaryNetworkIds) {
			manager.removeNetwork(networkId);
		}
		temporaryNetworkIds.clear();

		// release wifi lock
		lock.release();
		lock = null;

		// unregister broadcast receiver
		context.unregisterReceiver(receiver);

		// reconnect to previous network if any, else disconnect from
		// current network
		if (previouslyConnectedNetworkId != -1) {
			manager.enableNetwork(previouslyConnectedNetworkId, true);
		} else if (isConnected())
			manager.disconnect();

		sessionStarted = false;
	}

	@Override
	public synchronized void startSession() throws WifiAdapterException {
		if (sessionStarted)
			throw new WifiAdapterException("a session has already been started");

		// save current network id
		previouslyConnectedNetworkId = -1;
		WifiInfo info = manager.getConnectionInfo();
		if (info != null) {
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

	@Override
	public synchronized boolean isSessionStarted() throws WifiAdapterException {
		return sessionStarted;
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
		if (wifiConfig.allowedKeyManagement.get(KeyMgmt.WPA_PSK))
			return WifiSecurity.WPA_PSK;
		else if (wifiConfig.allowedGroupCiphers.get(GroupCipher.WEP40))
			return WifiSecurity.WEP_40;
		else if (wifiConfig.allowedGroupCiphers.get(GroupCipher.WEP104))
			return WifiSecurity.WEP_104;
		else
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
			if (matchConfiguration(wifiConfiguration, network)) {
				if (Logger.D)
					L.d("using existing WifiConfiguration for network "
							+ network);
				return wifiConfiguration;
			}
		}

		// network is not configured, we have to create a temporary
		// configuration
		if (Logger.D)
			L.d("creating new WifiConfiguration for network " + network);

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
				wifiConfiguration.allowedGroupCiphers.set(GroupCipher.WEP40);
				wifiConfiguration.allowedGroupCiphers.set(GroupCipher.WEP104);
				wifiConfiguration.allowedPairwiseCiphers
						.set(PairwiseCipher.NONE);
				wifiConfiguration.wepTxKeyIndex = 0;
				break;
			case WPA_PSK:
			case WPA2_PSK:
				if (!TextUtils.isEmpty(p)) {
					wifiConfiguration.allowedProtocols.set(Protocol.WPA);
					wifiConfiguration.allowedProtocols.set(Protocol.RSN);
					wifiConfiguration.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
					wifiConfiguration.allowedPairwiseCiphers
							.set(PairwiseCipher.TKIP);
					wifiConfiguration.allowedPairwiseCiphers
							.set(PairwiseCipher.CCMP);
					wifiConfiguration.allowedGroupCiphers.set(GroupCipher.TKIP);
					wifiConfiguration.allowedGroupCiphers.set(GroupCipher.CCMP);
					wifiConfiguration.allowedGroupCiphers
							.set(GroupCipher.WEP104);
					wifiConfiguration.allowedGroupCiphers
							.set(GroupCipher.WEP40);
					wifiConfiguration.allowedAuthAlgorithms
							.set(AuthAlgorithm.OPEN);

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
		if (networkId == -1) {
			if (Logger.D)
				L.d("cannot create WifiConfiguration for network " + network);
			return null;
		}
		wifiConfiguration.networkId = networkId;
		if (Logger.D)
			L.d("new WifiConfiguration created for network " + network
					+ " with id " + networkId);

		temporaryNetworkIds.add(networkId);
		// if (!manager.saveConfiguration())
		// return null;

		return wifiConfiguration;
	}

	private void prepareReachableNetworks() {
		reachableNetworks = null;

		List<ScanResult> scanResults = manager.getScanResults();

		if (scanResults != null) {

			if (Logger.V) {
				// build a list of scanResults and log it
				StringBuilder sb = new StringBuilder();
				sb.append("Scan results:\n");
				for (ScanResult scanResult : scanResults) {
					sb.append(scanResult);
					sb.append("\n");
				}
				L.v(sb.toString());
			}

			// TODO implement a finer grain control over network equality
			Set<String> seenBSSIDs = new HashSet<String>();

			List<WifiNetwork> reachableNetworksList = new ArrayList<WifiNetwork>();
			for (ScanResult scanResult : scanResults) {
				WifiNetwork network = createFromScanResult(scanResult);
				if (network != null && !seenBSSIDs.contains(network.getBSSID())) {
					reachableNetworksList.add(network);
					seenBSSIDs.add(network.getBSSID());
				}
			}

			reachableNetworks = reachableNetworksList
					.toArray(new WifiNetwork[reachableNetworksList.size()]);

			if (Logger.V) {
				// build a list of purged reachable networks list
				StringBuilder sb = new StringBuilder();
				sb.append("Reachable networks:\n");
				for (WifiNetwork network : reachableNetworksList) {
					sb.append(network);
					sb.append("\n");
				}
				L.v(sb.toString());
			}
		}
	}

	private synchronized void handleScanResultsAvailable() {
		// scan terminated, results available
		adapterState = AdapterState.IDLE;
		prepareReachableNetworks();
		getListener().scanCompleted();
	}

	private synchronized void handleNetworkStateChange(NetworkInfo info,
			String BSSID) {

		if (Logger.V) {
			WifiInfo wifi = manager.getConnectionInfo();
			StringBuilder sb = new StringBuilder();
			sb.append("NETWORK_STATE_CHANGE");
			sb.append("\n\tAdapter state: " + adapterState);
			sb.append("\n\tBSSID: ");
			if (!TextUtils.isEmpty(BSSID))
				sb.append(BSSID);
			else
				sb.append("unknown");
			sb.append("\n\tState: ");
			sb.append(info.getState());
			sb.append("\n\tDetailed state: ");
			sb.append(info.getDetailedState());
			sb.append("\n\tExtra: ");
			sb.append(info.getExtraInfo());
			sb.append("\n\tReason: ");
			sb.append(info.getReason());
			sb.append("\n\tFailover: ");
			sb.append(info.isFailover());
			if (wifi != null) {
				sb.append("\n\tWifi info:");
				sb.append("\n\t\tSupplicant state: ");
				sb.append(wifi.getSupplicantState());
				sb.append("\n\t\tDetailed supplicant state: ");
				sb.append(WifiInfo
						.getDetailedStateOf(wifi.getSupplicantState()));
				if (!TextUtils.isEmpty(wifi.getBSSID())) {
					sb.append("\n\t\tBSSID: ");
					sb.append(wifi.getBSSID());
				}
				if (!TextUtils.isEmpty(wifi.getSSID())) {
					sb.append("\n\t\tSSID: ");
					sb.append(wifi.getSSID());
				}
				if (wifi.getNetworkId() != -1) {
					sb.append("\n\t\tNetwork id: ");
					sb.append(wifi.getNetworkId());
				}
			}
			L.v(sb.toString());
		}

		if (adapterState == AdapterState.CONNECTING) {
			DetailedState state = info.getDetailedState();

			if (state == DetailedState.DISCONNECTED
					|| state == DetailedState.FAILED) {
				if (BSSID == null
						|| (connectingTo != null && connectingTo.getBSSID()
								.equals(BSSID))) {
					adapterState = AdapterState.IDLE;
					connectingTo = null;
					getListener().connectionFailed(null);
				}
			} else if (state == DetailedState.CONNECTED && BSSID != null
					&& connectingTo != null
					&& connectingTo.getBSSID().equals(BSSID)) {
				adapterState = AdapterState.IDLE;
				connectingTo = null;
				getListener().connectionCompleted();
			}
		}
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

		WifiSecurity security = getWifiConfigurationSecurity(wifiConfiguration);

		if (Logger.D) {
			L.d("matching network (SSID " + network.getSSID() + ", BSSID "
					+ network.getBSSID() + ", Security "
					+ network.getSecurity() + ") with configuration (SSID "
					+ SSID + ", BSSID " + wifiConfiguration.BSSID
					+ ", Security " + security + ")");
		}

		return matchNetworkData(network, SSID, wifiConfiguration.BSSID,
				security);
	}

	private boolean matchConnection(WifiInfo connection, WifiNetwork network) {
		if (connection.getSupplicantState() != SupplicantState.COMPLETED
				|| connection.getNetworkId() == -1)
			return false;
		WifiConfiguration configuration = findConfiguration(connection
				.getNetworkId());
		if (configuration == null)
			return false;
		String SSID = connection.getSSID();
		if (SSID == null)
			SSID = configuration.SSID;
		// remove quotes from config SSID
		if (!TextUtils.isEmpty(SSID)) {
			if (Utils.isQuotedString(SSID)) {
				SSID = Utils.convertToUnquotedString(SSID);
			}
		}
		String BSSID = connection.getBSSID();
		if (BSSID == null)
			BSSID = configuration.BSSID;
		return matchNetworkData(network, SSID, BSSID,
				getWifiConfigurationSecurity(configuration));
	}

	private static boolean matchNetworkData(WifiNetwork network, String SSID,
			String BSSID, WifiSecurity security) {
		if (!network.getSSID().equals(SSID))
			return false;
		if (!network.getBSSID().equals(BSSID))
			return false;
		if (network.getSecurity() != security)
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
