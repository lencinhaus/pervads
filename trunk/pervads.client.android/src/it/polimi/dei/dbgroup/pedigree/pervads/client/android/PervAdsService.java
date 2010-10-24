package it.polimi.dei.dbgroup.pedigree.pervads.client.android;

import it.polimi.dei.dbgroup.pedigree.pervads.client.android.preference.Settings;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Logger;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Utils;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi.ConnectionInfo;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi.IWifiAdapter;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi.IWifiAdapterListener;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi.WifiAdapterException;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi.WifiNetwork;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi.WifiSecurity;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi.android.AndroidWifiAdapter;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi.windows.WindowsNativeWifiAdapter;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.IPervAdsService;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.IPervAdsServiceListener;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;

public class PervAdsService extends Service implements IWifiAdapterListener {
	private static final Logger L = new Logger(PervAdsService.class
			.getSimpleName());
	public static final int PERVADS_NOTIFICATION_ID = 1;
	public static final String IGNORE_AUTO_UPDATE_EXTRA = "ignoreAutoUpdate";
	public static final String SERVICE_STARTED_ACTION = "it.polimi.dei.dbgroup.pedigree.pervads.service_started";
	public static final String SERVICE_STOPPED_ACTION = "it.polimi.dei.dbgroup.pedigree.pervads.service_stopped";

	private static final Intent SERVICE_STARTED_INTENT = new Intent(
			SERVICE_STARTED_ACTION);
	private static final Intent SERVICE_STOPPED_INTENT = new Intent(
			SERVICE_STOPPED_ACTION);

	private static final int CONNECT_TO_NEXT_NETWORK_MESSAGE = 1;
	private static final int START_UPDATE_MESSAGE = 2;

	private RemoteCallbackList<IPervAdsServiceListener> listeners = new RemoteCallbackList<IPervAdsServiceListener>();

	private final IWifiAdapter[] WIFI_ADAPTERS = new IWifiAdapter[] {
			new AndroidWifiAdapter(this, this),
			new WindowsNativeWifiAdapter(this) };

	private IWifiAdapter adapter = null;

	private boolean adapterEnabled = false;

	private int bindingsCounter = 0;

	private Settings settings = null;

	private boolean updating = false;

	private long lastUpdateStartTime = 0;

	private Map<String, List<PervAd>> pervAdsMap = new HashMap<String, List<PervAd>>();

	private PervAd[] pervAds = new PervAd[0];

	private WifiNetwork[] networks = null;
	private int currentNetworkId;
	// TODO not needed anymore, connection status is managed by adapter sessions
	// private WifiNetwork current = null;

	private final Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case CONNECT_TO_NEXT_NETWORK_MESSAGE:
					connectToNextNetwork();
					break;
				case START_UPDATE_MESSAGE:
					startUpdate();
					break;
				default:
					super.handleMessage(msg);
			}
		}

	};

	private final IPervAdsService.Stub binder = new IPervAdsService.Stub() {

		@Override
		public void startUpdate() throws RemoteException {
			PervAdsService.this.startUpdate();
		}

		@Override
		public PervAd[] getPervAds() throws RemoteException {
			return PervAdsService.this.getPervAds();
		}

		@Override
		public void pervAdSeen(String pervAdId) throws RemoteException {
			PervAdsService.this.pervAdSeen(pervAdId);
		}

		@Override
		public boolean isSupported() throws RemoteException {
			return PervAdsService.this.isSupported();
		}

		@Override
		public boolean isEnabled() throws RemoteException {
			return PervAdsService.this.isEnabled();
		}

		@Override
		public boolean isUpdating() throws RemoteException {
			return PervAdsService.this.isUpdating();
		}

		@Override
		public void registerListener(IPervAdsServiceListener listener)
				throws RemoteException {
			PervAdsService.this.registerListener(listener);
		}

		@Override
		public void unregisterListener(IPervAdsServiceListener listener)
				throws RemoteException {
			PervAdsService.this.unregisterListener(listener);
		}

	};

	@Override
	public void scanCompleted() {
		// TODO removed, this should be done during adapter's startSession
		// get current connection (will be restored after the scan)
		// current = adapter.getConnectedNetwork();

		if (Logger.D)
			L.d("network scan completed");

		// get reachable networks
		try {
			networks = adapter.getReachableNetworks();
		} catch (WifiAdapterException ex) {
			logAdapterException(ex,
					"an error occurred while retrieving reachable networks");
			fireNetworkScanEvent(EventTypes.Failed, 0);
			updateFailed();
			return;
		}

		int numFoundNetworks = networks.length;

		if (Config.TESTING_BEHAVIOR) {
			numFoundNetworks = 0;
			for (WifiNetwork network : networks) {
				if (Config.TESTING_SSID.equals(network.getSSID()))
					numFoundNetworks++;
			}
		}

		// notify listeners about scan completed
		fireNetworkScanEvent(EventTypes.Completed, numFoundNetworks);

		if (networks != null && networks.length > 0) {
			currentNetworkId = -1;
			handler.sendEmptyMessage(CONNECT_TO_NEXT_NETWORK_MESSAGE);
		} else
			updateCompleted();
	}

	@Override
	public void disconnectionCompleted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnectionFailed(WifiAdapterException ex) {
		// TODO Auto-generated method stub

	}

	@Override
	public void scanFailed(WifiAdapterException ex) {
		logAdapterException(ex, "an error occurred during network scan");

		// notify listeners about scan fail
		fireNetworkScanEvent(EventTypes.Failed, 0);

		updateFailed();
	}

	@Override
	public void connectionFailed(WifiAdapterException ex) {
		final String networkName = networks[currentNetworkId].getSSID();
		logAdapterException(ex, "connection to network " + networkName
				+ " failed", true);

		// notify listeners about connection fail
		fireNetworkConnectionEvent(EventTypes.Failed, networkName);

		handler.sendEmptyMessage(CONNECT_TO_NEXT_NETWORK_MESSAGE);
	}

	@Override
	public void connectionCompleted() {
		final WifiNetwork network = networks[currentNetworkId];
		final String networkName = network.getSSID();
		if (Logger.D)
			L.d("connection to network " + networkName + " completed");

		// notify listeners about connection completed
		fireNetworkConnectionEvent(EventTypes.Completed, networkName);

		// get current network pervads list
		final String networkId = network.getBSSID();
		List<PervAd> pervAdsList = pervAdsMap.get(networkId);
		if (pervAdsList == null) {
			// create a list for current network
			pervAdsList = new ArrayList<PervAd>();
			pervAdsMap.put(networkId, pervAdsList);
		}

		// download ontology from gateway

		try {
			int gateway = 0;

			if (Config.TESTING_BEHAVIOR) {
				gateway = Config.TESTING_GATEWAY;
			} else {
				L.d("retrieving connection data for network " + networkName);
				ConnectionInfo info = adapter.getConnectionInfo();
				L.d("connection data retrieved for network " + networkName);
				gateway = info.getGateway();
			}

			HttpParams params = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(params, 5000);
			HttpConnectionParams.setSoTimeout(params, 5000);
			HttpClient client = new DefaultHttpClient(params);
			String uri = "http://" + Utils.formatIPAddress(gateway)
					+ "/pervads.n3";

			if (Logger.D)
				L.d("sending a pervad request on URI " + uri + " to network "
						+ networkName);
			try {
				HttpGet request = new HttpGet(uri);
				HttpResponse response = client.execute(request);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					String encoding = null;
					Header encHeader = entity.getContentEncoding();
					if (encHeader != null) {
						encoding = encHeader.getValue();
					}

					if (Logger.D)
						L.d("received a response with "
								+ (TextUtils.isEmpty(encoding) ? "undefined"
										: encoding)
								+ " encoding, reading content");
					InputStreamReader isReader = null;
					if (!TextUtils.isEmpty(encoding)
							&& Charset.isSupported(encoding))
						isReader = new InputStreamReader(entity.getContent(),
								Charset.forName(encoding));
					else
						isReader = new InputStreamReader(entity.getContent());
					BufferedReader reader = new BufferedReader(isReader);
					StringBuilder sb = new StringBuilder();
					String s = null;
					while ((s = reader.readLine()) != null) {
						sb.append(s + "\n");
					}
					reader.close();
					s = sb.toString();

					if (Logger.D)
						L.d("response content read, adding pervads to results");

					// separate tokens
					String tokens[] = s.split("\n");
					int numPervAds = tokens.length / 2;
					for (int i = 0; i < numPervAds; i++) {
						String id = tokens[2 * i];
						String content = tokens[2 * i + 1];
						addPervAd(id, networkName, content, pervAdsList);
					}
				}
			} catch (Exception ex) {
				if (Logger.W)
					L.w(
							"an error occurred while sending a pervad request on URI "
									+ uri + " to network " + networkName, ex);
			}
		} catch (WifiAdapterException ex) {
			logAdapterException(ex,
					"an error occurred while retrieving connection data for network "
							+ networkName, true);
		}

		handler.sendEmptyMessage(CONNECT_TO_NEXT_NETWORK_MESSAGE);
	}

	private void addPervAd(String id, String networkName, String content,
			List<PervAd> pervAdsList) {
		// TODO mettere a posto sta roba quando si avrà il matching
		// ontologico vero
		boolean newPervAd = true;
		PervAd pervAd = null;
		for (PervAd oldPervAd : pervAdsList) {
			if (oldPervAd.getId().equals(id)) {
				if (oldPervAd.getContent().equals(content))
					newPervAd = false;
				else
					pervAd = oldPervAd;
				break;
			}
		}

		if (newPervAd) {
			if (pervAd == null) {
				pervAd = new PervAd(id, networkName, content, System
						.currentTimeMillis());
				pervAdsList.add(pervAd);
			} else {
				pervAd.setContent(content);
				pervAd.setFindTime(System.currentTimeMillis());
			}
			pervAd.setSeen(false);
		}
	}

	private void connectToNextNetwork() {
		while (++currentNetworkId < networks.length) {
			final WifiNetwork network = networks[currentNetworkId];
			final String networkName = network.getSSID();

			if (Config.TESTING_BEHAVIOR) {
				if (!Config.TESTING_SSID.equalsIgnoreCase(networkName))
					continue;
			}

			// notify listeners about connection start
			fireNetworkConnectionEvent(EventTypes.Started, networkName);

			try {
				if (Logger.D)
					L.d("trying to connect to network " + networkName);
				network.setPassword(choosePassword(network.getSecurity()));
				adapter.connect(network);
				break;
			} catch (WifiAdapterException ex) {
				logAdapterException(ex,
						"an error occurred while connecting to network "
								+ networkName, true);

				// notify listeners about connection fail
				fireNetworkConnectionEvent(EventTypes.Failed, networkName);
			}
		}
		if (currentNetworkId == networks.length) {
			// TODO removed: this should be done in adapter's endSession
			// we finished our update
			// if current is not null, reconnect to that network
			// if (current != null) {
			// if (!adapter.connect(current))
			// Log.w(TAG, "cannot reconnect to original network "
			// + current.getSSID());
			// }

			updateCompleted();
		}
	}

	private static final String choosePassword(WifiSecurity sec) {
		if (sec == WifiSecurity.WEP_40 || sec == WifiSecurity.WEP_SHARED_40
				|| sec == WifiSecurity.WEP)
			return Config.WEP40_PASSWORD;
		else if (sec == WifiSecurity.WEP_104
				|| sec == WifiSecurity.WEP_SHARED_104)
			return Config.WEP104_PASSWORD;
		else if (sec == WifiSecurity.WPA_PSK || sec == WifiSecurity.WPA2_PSK)
			return Config.WPA_PSK_PASSWORD;
		// default choice
		return Config.WPA_PSK_PASSWORD;
	}

	public synchronized boolean startUpdate() {
		if (updating)
			return false;

		// notify listeners about update start
		fireUpdateEvent(EventTypes.Started);

		updating = true;
		lastUpdateStartTime = System.currentTimeMillis();

		if (Logger.D)
			L.d("starting update");
		if (adapter == null) {
			if (Logger.D)
				L.d("no adapter, cannot start update");
			return false;
		}
		if (Logger.D)
			L.d("checking if adapter is enabled");
		try {
			if (!adapter.isEnabled()) {
				if (Logger.D)
					L.d("adapter is not enabled");
				return false;
			} else {
				if (Logger.D)
					L.d("adapter is enabled");
			}
		} catch (WifiAdapterException ex) {
			logAdapterException(ex,
					"an error occurred while checking if adapter is enabled");
			return false;
		}
		if (Logger.D)
			L.d("starting adapter session");
		try {
			adapter.startSession();
			if (Logger.D)
				L.d("adapter session started");
		} catch (WifiAdapterException ex) {
			logAdapterException(ex,
					"an error occurred while starting an adapter session");
			return false;
		}
		if (Logger.D)
			L.d("starting network scan");

		try {
			adapter.startNetworkScan();

			// notify listeners about network scan start
			fireNetworkScanEvent(EventTypes.Started, 0);
			if (Logger.D)
				L.d("network scan started");
		} catch (WifiAdapterException ex) {
			logAdapterException(ex,
					"an error occurred while starting a network scan");
			return false;
		}
		if (Logger.D)
			L.d("update started");

		return true;
	}

	public void registerListener(IPervAdsServiceListener listener) {
		if (listener != null)
			listeners.register(listener);
	}

	public void unregisterListener(IPervAdsServiceListener listener) {
		if (listener != null)
			listeners.unregister(listener);
	}

	private void updateCompleted() {
		if (Logger.D)
			L.d("update completed");
		if (Logger.D)
			L.d("ending adapter session");
		// end session
		try {
			adapter.endSession();
			if (Logger.D)
				L.d("adapter session ended");
		} catch (WifiAdapterException ex) {
			logAdapterException(ex,
					"An error occurred while ending adapter session after successful update");
		}

		// create final pervads array
		int total = 0;
		for (List<PervAd> pervAdsList : pervAdsMap.values()) {
			total += pervAdsList.size();
		}
		pervAds = new PervAd[total];
		int i = 0;
		for (List<PervAd> pervAdsList : pervAdsMap.values()) {
			for (PervAd pervAd : pervAdsList) {
				pervAds[i++] = pervAd;
			}
		}

		// send notifications
		if (shouldSendNotifications())
			sendNewPervAdsNotifications();

		onUpdateFinished();

		// notify listeners about update completed
		fireUpdateEvent(EventTypes.Completed);
	}

	private void updateFailed() {
		if (Logger.W)
			L.w("update failed");
		// end session
		try {
			adapter.endSession();
		} catch (WifiAdapterException ex) {
			logAdapterException(ex,
					"An error occurred while ending adapter session after a failed update");
		}

		onUpdateFinished();

		// notify listeners about update fail
		fireUpdateEvent(EventTypes.Failed);
	}

	private void onUpdateFinished() {
		updating = false;

		if (shouldAutoPostUpdate()) {
			postNextUpdate();
		}
	}

	private boolean shouldAutoPostUpdate() {
		if (settings.getAutoUpdate()) {
			if (bindingsCounter == 0) {
				if (Logger.V)
					L
							.v("should post an update because auto-update is enabled and there are no active bindings");
				return true;
			} else {
				if (Logger.V)
					L
							.v("should not post an update even if auto-update is enabled, because there are active bindings");
			}
		} else {
			if (Logger.V)
				L
						.v("should not post an update because auto-update is disabled");
		}
		return false;
	}

	private boolean shouldSendNotifications() {
		if (bindingsCounter == 0) {
			if (Logger.V)
				L
						.v("should send notifications because there are no active bindings");
			return true;
		} else {
			if (Logger.V)
				L
						.v("should not send notifications because there are active bindings");
			return false;
		}
	}

	private void sendNewPervAdsNotifications() {
		// build ticker text
		PervAd[] pervAds = getPervAds();
		int numNewPervads = 0;
		StringBuilder contentSB = new StringBuilder();
		for (PervAd pervAd : pervAds) {
			if (!pervAd.isSeen()) {
				if (numNewPervads++ > 0)
					contentSB.append(", ");
				contentSB.append(pervAd.getId());
			}
		}
		if (numNewPervads > 0) {
			NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			if (manager == null) {
				if (Logger.W)
					L
							.w("cannot get notification manager to send new pervads notifications");
				return;
			}
			Notification n = new Notification(
					R.drawable.stat_notify_sms,
					String
							.format(
									getString(R.string.notification_new_pervads_ticker_text),
									numNewPervads), System.currentTimeMillis());
			n.flags |= Notification.FLAG_AUTO_CANCEL;
			n.defaults |= Notification.DEFAULT_SOUND;
			n.defaults |= Notification.DEFAULT_VIBRATE;
			n.number = numNewPervads;
			Intent ni = new Intent(this, PervAdsListActivity.class);
			PendingIntent pi = PendingIntent.getActivity(this, 0, ni, 0);
			n.setLatestEventInfo(getApplicationContext(), String.format(
					getString(R.string.notification_new_pervads_content_title),
					numNewPervads), contentSB.toString(), pi);
			manager.notify(PERVADS_NOTIFICATION_ID, n);
		}
	}

	private void removeNewPervAdsNotifications() {
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (manager == null) {
			if (Logger.W)
				L
						.w("cannot get notification manager to remove new pervads notifications");
			return;
		}

		manager.cancel(PERVADS_NOTIFICATION_ID);
	}

	private void postNextUpdate() {
		if (Logger.V)
			L.v("postNextUpdate()");

		long elapsedTime = System.currentTimeMillis() - lastUpdateStartTime;
		long interval = ((long) settings.getUpdateInterval()) * 60000L; // from
		// minutes
		// to
		// milliseconds
		if (elapsedTime < interval) {
			// delay the update
			long delay = interval - elapsedTime;
			if (Logger.D)
				L.d("posting a delayed update (delay " + delay + " ms)");
			handler.sendEmptyMessageDelayed(START_UPDATE_MESSAGE, delay);
		} else {
			// instantly start the update
			if (Logger.D)
				L.d("posting an immediate update");
			handler.sendEmptyMessage(START_UPDATE_MESSAGE);
		}
	}

	public PervAd[] getPervAds() {
		return pervAds;
	}

	public void pervAdSeen(String pervAdId) {
		for (PervAd oldPervAd : pervAds) {
			if (oldPervAd.getId().equals(pervAdId)) {
				oldPervAd.setSeen(true);
			}
		}
	}

	public boolean isSupported() {
		// if adapter is null, no adapter is supported
		return adapter != null;
	}

	public synchronized boolean isEnabled() {
		// if adapter is null, it can't be enabled
		if (adapter == null)
			return false;

		return adapterEnabled;
	}

	public boolean isUpdating() {
		return updating;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		if (Logger.V)
			L.v("onBind(" + arg0 + ")");

		onClientBound();

		return binder;
	}

	@Override
	public void onRebind(Intent intent) {
		if (Logger.V)
			L.v("onRebind(" + intent + ")");

		onClientBound();
	}

	private void onClientBound() {
		if (Logger.V)
			L.v("onClientBound()");

		// increase bindings count
		bindingsCounter++;

		// remove notifications
		removeNewPervAdsNotifications();

		// empty update queue
		handler.removeMessages(START_UPDATE_MESSAGE);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		super.onUnbind(intent);

		if (Logger.V)
			L.v("onUnbind(" + intent + ")");

		// if it's the case, post an
		// update
		bindingsCounter--;

		if (shouldAutoPostUpdate())
			postNextUpdate();

		// return true so that next calls to bindService will result in a call
		// to onRebind
		return true;
	}

	@Override
	public void onCreate() {
		if (Logger.V)
			L.v("onCreate()");

		// initialize settings
		settings = new Settings(this);

		initializeAdapter();
	}

	@Override
	public void onDestroy() {
		if (Logger.V)
			L.v("onDestroy()");
		super.onDestroy();

		// empty update queue
		handler.removeMessages(START_UPDATE_MESSAGE);

		// send stopped broadcast
		sendBroadcast(SERVICE_STOPPED_INTENT);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		if (Logger.V)
			L.v("onStart(" + intent + ", " + startId + ")");
		super.onStart(intent, startId);

		// send broadcast for service start
		sendBroadcast(SERVICE_STARTED_INTENT);

		// if autoupdate is active and there are no bindings, post an update
		boolean ignoreAutoUpdate = intent.getBooleanExtra(
				IGNORE_AUTO_UPDATE_EXTRA, false);
		if (Logger.V)
			L.v("ignoreAutoUpdate: " + ignoreAutoUpdate);
		if (!ignoreAutoUpdate) {
			if (shouldAutoPostUpdate())
				postNextUpdate();
		} else if (Logger.D)
			L
					.d("update not started because IGNORE_AUTO_UPDATE = true extra specified inside intent");
	}

	private void initializeAdapter() {
		adapter = null;
		for (IWifiAdapter candidate : WIFI_ADAPTERS) {
			final String adapterName = candidate.getClass().getName();
			if (Logger.V)
				L.v("initializing WiFi adapter " + adapterName);
			try {
				candidate.initialize();
			} catch (WifiAdapterException ex) {
				logAdapterException(ex,
						"an error occurred while initializing WiFi adapter "
								+ adapterName, true);
				continue;
			}
			if (Logger.V)
				L.v("WiFi adapter " + adapterName + " initialized");
			if (Logger.V)
				L
						.v("checking if WiFi adapter " + adapterName
								+ " is supported");
			try {
				if (candidate.isSupported()) {
					if (Logger.V)
						L.v("WiFi adapter " + adapterName + " is supported");
					adapter = candidate;
					break;
				} else if (Logger.V)
					L.v("WiFi adapter " + adapterName + " is not supported");
			} catch (WifiAdapterException ex) {
				logAdapterException(ex,
						"an error occurred while checking if WiFi adapter "
								+ adapterName + " is supported", true);
			}
		}

		if (adapter == null) {
			if (Logger.W)
				L.w("No WiFi adapter is supported, service is not supported");
		} else {
			try {
				adapterEnabled = adapter.isEnabled();
			} catch (WifiAdapterException ex) {
				if (Logger.W)
					L
							.w(
									"Cannot determine if adapter is enabled, setting enabled = false",
									ex);
				adapterEnabled = false;
			}

			if (Logger.I)
				L.i("Chosen WiFi adapter: " + adapter.getClass().getName());
		}

	}

	private void logAdapterException(WifiAdapterException ex, String message) {
		logAdapterException(ex, message, false);
	}

	private void logAdapterException(WifiAdapterException ex, String message,
			boolean isWarning) {
		// TODO log network data contained in the exception
		String fullMessage = message;
		if (adapter != null)
			fullMessage += ". Adapter: " + adapter.getClass().getName();
		if (isWarning)
			if (ex != null)
				if (Logger.W)
					L.w(fullMessage, ex);
				else if (Logger.W)
					L.w(fullMessage);
				else if (ex != null)
					if (Logger.E)
						L.e(fullMessage, ex);
					else if (Logger.E)
						L.e(fullMessage);
	}

	private void fireUpdateEvent(int eventType) {
		final int N = listeners.beginBroadcast();
		for (int i = 0; i < N; i++) {
			try {
				listeners.getBroadcastItem(i).updateEvent(eventType);
			} catch (RemoteException ex) {
				if (Logger.E)
					L.e(
							"an error occurred while notifying listeners about update event (type "
									+ EventTypes.describe(eventType) + ")", ex);
			}
		}
		listeners.finishBroadcast();
	}

	private void fireNetworkScanEvent(int eventType, int numFoundNetworks) {
		final int N = listeners.beginBroadcast();
		for (int i = 0; i < N; i++) {
			try {
				listeners.getBroadcastItem(i).networkScanEvent(eventType,
						numFoundNetworks);
			} catch (RemoteException ex) {
				if (Logger.E)
					L.e(
							"an error occurred while notifying listeners about network scan event (type "
									+ EventTypes.describe(eventType) + ")", ex);
			}
		}
		listeners.finishBroadcast();
	}

	private void fireNetworkConnectionEvent(int eventType, String SSID) {
		final int N = listeners.beginBroadcast();
		for (int i = 0; i < N; i++) {
			try {
				listeners.getBroadcastItem(i).networkConnectionEvent(eventType,
						SSID);
			} catch (RemoteException ex) {
				if (Logger.E)
					L
							.e(
									"an error occurred while notifying listeners about network connection event (type "
											+ EventTypes.describe(eventType)
											+ ", SSID: " + SSID + ")", ex);
			}
		}
		listeners.finishBroadcast();
	}
}
