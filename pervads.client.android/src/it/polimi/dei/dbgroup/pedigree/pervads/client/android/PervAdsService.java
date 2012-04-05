package it.polimi.dei.dbgroup.pedigree.pervads.client.android;

import it.polimi.dei.dbgroup.pedigree.pervads.client.android.data.AttachedMediaManager;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.data.HotspotAttachedMediaManager;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.data.HotspotClient;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.data.ServerDataManager;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.data.ServerDataSource;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.preference.Settings;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.query.QueryResult;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.query.ResultBuilder;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.query.ResultManager;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.InitializationManager;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.Html;

public class PervAdsService extends Service implements IWifiAdapterListener {
	private interface IListenerNotifier {
		public void notifyListener(IPervAdsServiceListener listener)
				throws RemoteException;
	}

	private final Runnable updateRunnable = new Runnable() {

		@Override
		public void run() {
			if (Logger.D)
				L.d("update started");

			// notify listeners about update start
			postUpdateNotification(EventTypes.Started);
			lastUpdateStartTime = System.currentTimeMillis();

			// initialize result builder
			if (Logger.D)
				L.d("initializing result builder");
			ResultBuilder resultBuilder = ResultBuilder.getInstance();
			InitializationManager.initializeSync(PervAdsService.this,
					resultBuilder);

			// check if the result builder can actually build any results (is
			// there an enabled query?)
			if (!resultBuilder.start()) {
				// no results can be built, no need to perform a network scan
				// stop the update now
				if (Logger.D)
					L
							.d("Result builder has no queries, completing update immediately");
				updateCompleted();
				return;
			}

			if (adapter == null) {
				if (Logger.D)
					L.d("no adapter, cannot update");
				updateFailed();
				return;
			}

			if (Logger.D)
				L.d("checking if adapter is enabled");
			try {
				if (!adapter.isEnabled()) {
					if (Logger.D)
						L.d("adapter is not enabled");
					updateFailed();
					return;
				}
			} catch (WifiAdapterException ex) {
				logAdapterException(ex,
						"an error occurred while checking if adapter was enabled");
				updateFailed();
				return;
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
				updateFailed();
				return;
			}
			if (Logger.D)
				L.d("starting network scan");

			try {
				adapter.startNetworkScan();
			} catch (WifiAdapterException ex) {
				logAdapterException(ex,
						"an error occurred while starting a network scan");
				updateFailed();
				return;
			}

			if (Logger.D)
				L.d("network scan started");

			// notify listeners about network scan start
			postNetworkScanNotification(EventTypes.Started, 0);
		}
	};

	private final Runnable connectionRunnable = new Runnable() {

		@Override
		public void run() {
			while (++currentNetworkId < networks.length) {
				final WifiNetwork network = networks[currentNetworkId];
				final String networkName = network.getSSID();
				
				if(Config.TESTING_BEHAVIOR && Config.TESTING_SSID != null && !networkName.equals(Config.TESTING_SSID)) {
					handler.sendEmptyMessage(CONNECT_TO_NEXT_NETWORK_MESSAGE);
					break;
				}

				// notify listeners about connection start
				postNetworkConnectionEvent(EventTypes.Started, networkName);

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
					postNetworkConnectionEvent(EventTypes.Failed, networkName);
				}
			}
			if (currentNetworkId == networks.length) {
				// we have finished connecting, complete the update
				updateCompleted();
			}
		}
	};

	private final Logger L = new Logger(PervAdsService.class.getSimpleName());
	private static final String PERVADS_DATA_FILE_NAME = "pervads.owl";
	public static final int PERVADS_NOTIFICATION_ID = 1;
	public static final String IGNORE_AUTO_UPDATE_EXTRA = "it.polimi.dei.dbgroup.pedigree.pervads.client.android.PervAdsService.ignoreAutoUpdate";
	public static final String SERVICE_STARTED_ACTION = "it.polimi.dei.dbgroup.pedigree.pervads.service_started";
	public static final String SERVICE_STOPPED_ACTION = "it.polimi.dei.dbgroup.pedigree.pervads.service_stopped";

	private static final Intent SERVICE_STARTED_INTENT = new Intent(
			SERVICE_STARTED_ACTION);
	private static final Intent SERVICE_STOPPED_INTENT = new Intent(
			SERVICE_STOPPED_ACTION);

	// Handler message codes
	private static final int START_UPDATE_MESSAGE = 1;
	private static final int NOTIFY_LISTENERS_MESSAGE = 2;
	private static final int CONNECT_TO_NEXT_NETWORK_MESSAGE = 3;

	private RemoteCallbackList<IPervAdsServiceListener> listeners = new RemoteCallbackList<IPervAdsServiceListener>();

	private final IWifiAdapter[] WIFI_ADAPTERS = new IWifiAdapter[] {
			new AndroidWifiAdapter(this, this),
			new WindowsNativeWifiAdapter(this) };

	private IWifiAdapter adapter = null;

	private ServerDataManager serverDataManager = null;

	private boolean adapterEnabled = false;

	private int bindingsCounter = 0;

	private Settings settings = null;

	private boolean updating = false;

	private long lastUpdateStartTime = 0;

	private WifiNetwork[] networks = null;
	private int currentNetworkId;

	private final Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case CONNECT_TO_NEXT_NETWORK_MESSAGE:
					new Thread(connectionRunnable).start();
					break;
				case START_UPDATE_MESSAGE:
					startUpdate();
					break;
				case NOTIFY_LISTENERS_MESSAGE:
					IListenerNotifier notifier = (IListenerNotifier) msg.obj;
					fireNotification(notifier);
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
		if (Logger.D)
			L.d("network scan completed");

		// get reachable networks
		try {
			networks = adapter.getReachableNetworks();
		} catch (WifiAdapterException ex) {
			scanFailed(ex);
			return;
		}

		int numFoundNetworks = networks.length;

		// disabled, connect to all reachable networks even in testing behavior
//		if (Config.TESTING_BEHAVIOR) {
//			numFoundNetworks = 0;
//			for (WifiNetwork network : networks) {
//				if (Config.TESTING_SSID.equals(network.getSSID()))
//					numFoundNetworks++;
//			}
//		}

		// notify listeners about scan completed
		postNetworkScanNotification(EventTypes.Completed, numFoundNetworks);

		// start server data manager update
		serverDataManager.startUpdate();

		if (networks != null && networks.length > 0) {
			currentNetworkId = -1;
			handler.sendEmptyMessage(CONNECT_TO_NEXT_NETWORK_MESSAGE);
		} else
			updateCompleted();
	}

	@Override
	public void scanFailed(WifiAdapterException ex) {
		logAdapterException(ex, "an error occurred during network scan");

		// notify listeners about scan fail
		postNetworkScanNotification(EventTypes.Failed, 0);

		updateFailed();
	}

	@Override
	public void connectionFailed(WifiAdapterException ex) {
		final String networkName = networks[currentNetworkId].getSSID();
		logAdapterException(ex, "connection to network " + networkName
				+ " failed", true);

		// notify listeners about connection fail
		postNetworkConnectionEvent(EventTypes.Failed, networkName);

		handler.sendEmptyMessage(CONNECT_TO_NEXT_NETWORK_MESSAGE);
	}

	@Override
	public void connectionCompleted() {
		final WifiNetwork network = networks[currentNetworkId];
		final String networkName = network.getSSID();
		if (Logger.D)
			L.d("connection to network " + networkName + " completed");

		// use BSSID as network univoque id
		final String networkId = network.getBSSID();

		// read connection data for this network
		try {
			if (Logger.D)
				L.d("reading connection data for network " + networkName);
			ConnectionInfo connection = adapter.getConnectionInfo();
			if (connection != null) {
				if (Logger.D)
					L.d("downloading content for network " + networkName);
				// download content from gateway
				HotspotClient client = HotspotClient.createFromConnection(connection);
				InputStream dataStream = client
						.download(PERVADS_DATA_FILE_NAME);
				if (dataStream != null) {
					if (Logger.D)
						L.d("processing content for network " + networkName);

					String networkFileName = Utils.getFilenameFromBSSID(networkId);
					// create an attached media manager for this network
					AttachedMediaManager mediaManager = new HotspotAttachedMediaManager(
							this, networkFileName, client);

					// add the content to server data manager
					ServerDataSource source = serverDataManager.addData(
							networkFileName, dataStream);

					// process it with result builder
					boolean processed = ResultBuilder.getInstance()
							.processContent(source.getStream(), mediaManager);

					try {
						dataStream.close();
					} catch (IOException ex) {
						L.w("Cannot close content data stream for network "
								+ networkName, ex);
					}

					if (!processed) {
						// the data is unprocessable, we can remove it from
						// server data manager
						serverDataManager.removeData(source.getKey());
						if (Logger.W)
							L.w("cannot process content for network "
									+ networkName);
					}
				} else {
					if (Logger.W)
						L.w("cannot download content for network "
								+ networkName);
				}
			}
		} catch (WifiAdapterException ex) {
			logAdapterException(ex,
					"an error occurred while retrieving connection data for network "
							+ networkName, true);
		}

		// notify listeners about connection completed
		postNetworkConnectionEvent(EventTypes.Completed, networkName);

		handler.sendEmptyMessage(CONNECT_TO_NEXT_NETWORK_MESSAGE);
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

	private void processCachedServerData() {
		int count = serverDataManager.countUnmodifiedData();
		if (Logger.D)
			L.d("processing " + count + " cached data");
		// notify listeners
		postCachedDataCountEvent(count);

		Iterator<ServerDataSource> it = serverDataManager.getUnmodifiedData();
		ResultBuilder resultBuilder = ResultBuilder.getInstance();
		while (it.hasNext()) {
			ServerDataSource source = it.next();
			if (Logger.D)
				L.d("starting processing data for source " + source.getKey());

			// notify listeners
			postCachedDataProcessingEvent(EventTypes.Started);

			InputStream dataStream = source.getStream();
			boolean processed = resultBuilder.processContent(dataStream,
					AttachedMediaManager.Offline);
			try {
				dataStream.close();
			} catch (IOException ex) {
				if (Logger.W)
					L.w("cannot close server data stream for source "
							+ source.getKey(), ex);
			}
			if (!processed) {
				if (Logger.W)
					L.w("cannot process data for source " + source.getKey()
							+ ", removing it from server data cache");

				it.remove();

				// notify listeners
				postCachedDataProcessingEvent(EventTypes.Failed);
			} else {
				if (Logger.D)
					L.d("finished processing data for source "
							+ source.getKey());
				// notify listeners
				postCachedDataProcessingEvent(EventTypes.Completed);
			}
		}
	}

	public synchronized boolean startUpdate() {
		if (updating)
			return false;

		updating = true;

		new Thread(updateRunnable).start();
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
		finishUpdate(EventTypes.Completed);
	}

	private void updateFailed() {
		finishUpdate(EventTypes.Failed);
	}

	private void finishUpdate(int eventType) {
		// end adapter session
		try {
			if (adapter.isSessionStarted())
				adapter.endSession();
		} catch (WifiAdapterException ex) {
			logAdapterException(ex,
					"An error occurred while ending adapter session");
		}

		if (serverDataManager.isUpdating()) {
			// process cached server data
			processCachedServerData();

			// stop updating server data manager
			serverDataManager.endUpdate();
		}

		// stop the result builder and update the results
		// TODO: check for duplicates, seen pervads etc
		ResultBuilder resultBuilder = ResultBuilder.getInstance();
		if (resultBuilder.isStarted()) {
			ResultManager resultManager = new ResultManager(this);
			resultManager.setResults(resultBuilder.stop());
		}

		// send notifications
		if (shouldSendNotifications())
			sendNewPervAdsNotifications();

		updating = false;

		if (eventType == EventTypes.Completed)
			if (Logger.D)
				L.d("update completed");
			else if (Logger.W)
				L.w("update failed");

		// notify listeners about update conclusion
		postUpdateNotification(eventType);

		// post the next update if needed
		if (shouldAutoPostUpdate())
			postNextUpdate();
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
		// PervAd[] pervAds = getPervAds();
		// int numNewPervads = 0;
		// StringBuilder contentSB = new StringBuilder();
		// for (PervAd pervAd : pervAds) {
		// if (!pervAd.isSeen()) {
		// if (numNewPervads++ > 0)
		// contentSB.append(", ");
		// contentSB.append(pervAd.getId());
		// }
		// }
		ResultManager resultManager = new ResultManager(this);
		int numNewPervads = 0;
		// StringBuilder contentSB = new StringBuilder();
		for (QueryResult result : resultManager.getResults()) {
			numNewPervads += result.getMatchingPervADs().size();
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
			n.setLatestEventInfo(getApplicationContext(),
					getText(R.string.notification_new_pervads_content_title),
					Html.fromHtml(getString(
							R.string.notification_new_pervads_content_text,
							numNewPervads)), pi);
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
		if (shouldAutoPostUpdate()) {
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

		// create server data manager
		serverDataManager = new ServerDataManager(this);
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
		if (isWarning && Logger.W) {
			if (ex != null)
				L.w(fullMessage, ex);
			else
				L.w(fullMessage);
		} else if (Logger.E) {
			if (ex != null)
				L.e(fullMessage, ex);
			else
				L.e(fullMessage);
		}
	}

	private void postUpdateNotification(final int eventType) {
		postNotification(new IListenerNotifier() {
			@Override
			public void notifyListener(IPervAdsServiceListener listener)
					throws RemoteException {
				listener.updateEvent(eventType);
			}
		});
	}

	private void postNetworkScanNotification(final int eventType,
			final int numFoundNetworks) {
		postNotification(new IListenerNotifier() {
			@Override
			public void notifyListener(IPervAdsServiceListener listener)
					throws RemoteException {
				listener.networkScanEvent(eventType, numFoundNetworks);
			}
		});
	}

	private void postNetworkConnectionEvent(final int eventType,
			final String SSID) {
		postNotification(new IListenerNotifier() {

			@Override
			public void notifyListener(IPervAdsServiceListener listener)
					throws RemoteException {
				listener.networkConnectionEvent(eventType, SSID);
			}
		});
	}

	private void postCachedDataCountEvent(final int count) {
		postNotification(new IListenerNotifier() {

			@Override
			public void notifyListener(IPervAdsServiceListener listener)
					throws RemoteException {
				listener.cachedDataCountEvent(count);
			}
		});
	}

	private void postCachedDataProcessingEvent(final int eventType) {
		postNotification(new IListenerNotifier() {

			@Override
			public void notifyListener(IPervAdsServiceListener listener)
					throws RemoteException {
				listener.cachedDataProcessingEvent(eventType);
			}
		});
	}

	private void postNotification(IListenerNotifier notifier) {
		handler.obtainMessage(NOTIFY_LISTENERS_MESSAGE, notifier)
				.sendToTarget();
	}

	private void fireNotification(IListenerNotifier notifier) {
		final int N = listeners.beginBroadcast();
		for (int i = 0; i < N; i++) {
			try {
				notifier.notifyListener(listeners.getBroadcastItem(i));
			} catch (RemoteException ex) {
				if (Logger.E)
					L.e("an error occurred while notifying listeners", ex);
			}
		}
		listeners.finishBroadcast();
	}
}
