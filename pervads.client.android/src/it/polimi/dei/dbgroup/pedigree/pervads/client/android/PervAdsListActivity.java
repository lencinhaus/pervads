package it.polimi.dei.dbgroup.pedigree.pervads.client.android;

import it.polimi.dei.dbgroup.pedigree.pervads.client.android.adapters.QueryResultAdapter;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Logger;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Utils;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class PervAdsListActivity extends ListActivity {
	private static final Logger L = new Logger(PervAdsListActivity.class
			.getSimpleName());

	private static final int UPDATE_STARTED_MESSAGE = 1;
	private static final int UPDATE_COMPLETED_MESSAGE = 2;
	private static final int UPDATE_FAILED_MESSAGE = 3;
	private static final int NETWORK_SCAN_STARTED_MESSAGE = 4;
	private static final int NETWORK_SCAN_COMPLETED_MESSAGE = 5;
	private static final int NETWORK_SCAN_FAILED_MESSAGE = 6;
	private static final int NETWORK_CONNECTION_STARTED_MESSAGE = 7;
	private static final int NETWORK_CONNECTION_COMPLETED_MESSAGE = 8;
	private static final int NETWORK_CONNECTION_FAILED_MESSAGE = 9;
	private static final int CACHED_DATA_COUNT_MESSAGE = 10;
	private static final int CACHED_DATA_PROCESSING_STARTED_MESSAGE = 11;
	private static final int CACHED_DATA_PROCESSING_COMPLETED_MESSAGE = 12;
	private static final int CACHED_DATA_PROCESSING_FAILED_MESSAGE = 13;

	private QueryResultAdapter resultAdapter;
	private Button updateButton;
	private ProgressDialog progressDialog = null;
	private IPervAdsService pervAdsService = null;

	private final Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case UPDATE_STARTED_MESSAGE:
					updateStarted();
					break;
				case UPDATE_COMPLETED_MESSAGE:
					updateCompleted();
					break;
				case UPDATE_FAILED_MESSAGE:
					updateFailed();
					break;
				case NETWORK_SCAN_STARTED_MESSAGE:
					networkScanStarted();
					break;
				case NETWORK_SCAN_COMPLETED_MESSAGE:
					networkScanCompleted(msg.arg1);
					break;
				case NETWORK_SCAN_FAILED_MESSAGE:
					networkScanFailed();
					break;
				case NETWORK_CONNECTION_STARTED_MESSAGE:
				case NETWORK_CONNECTION_COMPLETED_MESSAGE:
				case NETWORK_CONNECTION_FAILED_MESSAGE:
					String SSID = (String) msg.obj;
					switch (msg.what) {
						case NETWORK_CONNECTION_STARTED_MESSAGE:
							networkConnectionStarted(SSID);
							break;
						case NETWORK_CONNECTION_COMPLETED_MESSAGE:
							networkConnectionCompleted(SSID);
							break;
						case NETWORK_CONNECTION_FAILED_MESSAGE:
							networkConnectionFailed(SSID);
							break;
					}
					break;
				case CACHED_DATA_PROCESSING_STARTED_MESSAGE:
					cachedDataProcessingStarted();
					break;
				case CACHED_DATA_PROCESSING_COMPLETED_MESSAGE:
					cachedDataProcessingCompleted();
					break;
				case CACHED_DATA_PROCESSING_FAILED_MESSAGE:
					cachedDataProcessingFailed();
					break;
				case CACHED_DATA_COUNT_MESSAGE:
					cachedDataCount(msg.arg1);
					break;
				default:
					super.handleMessage(msg);
					break;
			}
		}
	};

	private Runnable updateStarter = new Runnable() {

		@Override
		public void run() {
			try {
				if (Logger.D)
					L.d("starting an update");
				pervAdsService.startUpdate();
			} catch (RemoteException ex) {
				if (Logger.E)
					L.e("error while communicating with service", ex);
				updateFailed();
			}
		}
	};

	private final ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			if (Logger.V)
				L.v("onServiceDisconnected(" + name + ")");
			pervAdsService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			pervAdsService = IPervAdsService.Stub.asInterface(service);
			if (Logger.V)
				L.v("ServiceConnection.onServiceConnected(" + name + ", "
						+ service + ")");

			// check if service is supported
			try {
				if (Logger.V)
					L.v("checking if service is supported");
				if (pervAdsService.isSupported()) {
					// check if service is enabled
					if (Logger.V)
						L.v("checking if service is enabled");
					if (pervAdsService.isEnabled()) {
						// register listener
						if (Logger.V)
							L.v("registering service listener");
						pervAdsService.registerListener(serviceListener);

						// check if updating
						if (Logger.V)
							L.v("checking if service is updating");
						if (pervAdsService.isUpdating()) {
							// service is already updating, so just wait for
							// update completed notification
							setControlsEnabled(false);
							setUpdating(true);
							updateStarted();
						} else {
							// service is not updating

							setUpdating(false);
							setControlsEnabled(true);
						}
					} else {
						// TODO service is not enabled, notify user
						if (Logger.I)
							L.i("service is not enabled");
					}
				} else {
					// TODO service is not supported, notify user
					if (Logger.W)
						L.w("service is not supported");
				}
			} catch (RemoteException ex) {
				// TODO notify user about communication error
				if (Logger.E)
					L
							.e(
									"an error occurred while communicating with pervads service",
									ex);
			}
		}
	};

	private final IPervAdsServiceListener serviceListener = new IPervAdsServiceListener.Stub() {

		@Override
		public void updateEvent(int type) throws RemoteException {
			if (Logger.V)
				L.v("updateEvent(" + EventTypes.describe(type) + ")");

			switch (type) {
				case EventTypes.Started:
					handler.sendEmptyMessage(UPDATE_STARTED_MESSAGE);
					break;
				case EventTypes.Completed:
					handler.sendEmptyMessage(UPDATE_COMPLETED_MESSAGE);
					break;
				case EventTypes.Failed:
					handler.sendEmptyMessage(UPDATE_FAILED_MESSAGE);
					break;
			}
		}

		@Override
		public void networkConnectionEvent(int type, String SSID)
				throws RemoteException {
			if (Logger.V)
				L.v("networkConnectionEvent(" + EventTypes.describe(type)
						+ ", " + SSID + ")");

			switch (type) {
				case EventTypes.Started:
					handler.obtainMessage(NETWORK_CONNECTION_STARTED_MESSAGE,
							SSID).sendToTarget();
					break;
				case EventTypes.Completed:
					handler.obtainMessage(NETWORK_CONNECTION_COMPLETED_MESSAGE,
							SSID).sendToTarget();
					break;
				case EventTypes.Failed:
					handler.obtainMessage(NETWORK_CONNECTION_FAILED_MESSAGE,
							SSID).sendToTarget();
					break;
			}
		}

		@Override
		public void networkScanEvent(int type, int numFoundNetworks)
				throws RemoteException {
			if (Logger.V)
				L.v("networkScanEvent(" + EventTypes.describe(type) + ", "
						+ numFoundNetworks + ")");

			switch (type) {
				case EventTypes.Started:
					handler.obtainMessage(NETWORK_SCAN_STARTED_MESSAGE,
							numFoundNetworks, 0).sendToTarget();
					break;
				case EventTypes.Completed:
					handler.obtainMessage(NETWORK_SCAN_COMPLETED_MESSAGE,
							numFoundNetworks, 0).sendToTarget();
					break;
				case EventTypes.Failed:
					handler.obtainMessage(NETWORK_SCAN_FAILED_MESSAGE,
							numFoundNetworks, 0).sendToTarget();
					break;
			}
		}

		@Override
		public void cachedDataProcessingEvent(int type) throws RemoteException {
			if (Logger.V)
				L.v("cachedDataProcessingEvent(" + EventTypes.describe(type)
						+ ")");

			switch (type) {
				case EventTypes.Started:
					handler
							.sendEmptyMessage(CACHED_DATA_PROCESSING_STARTED_MESSAGE);
					break;
				case EventTypes.Completed:
					handler
							.sendEmptyMessage(CACHED_DATA_PROCESSING_COMPLETED_MESSAGE);
					break;
				case EventTypes.Failed:
					handler
							.sendEmptyMessage(CACHED_DATA_PROCESSING_FAILED_MESSAGE);
					break;
			}
		}

		@Override
		public void cachedDataCountEvent(int count) throws RemoteException {
			if (Logger.V)
				L.v("cachedDataCountEvent(" + count + ")");

			handler.obtainMessage(CACHED_DATA_COUNT_MESSAGE, count, 0)
					.sendToTarget();
		}
	};

	private final OnClickListener updateButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			startUpdate();
		}
	};

	// private final OnItemClickListener pervAdClickListener = new
	// OnItemClickListener() {
	//
	// @Override
	// public void onItemClick(AdapterView<?> parent, View view, int position,
	// long id) {
	// PervAd pervAd = listAdapter.getPervAd(position);
	// Toast.makeText(
	// PervAdsListActivity.this,
	// "Network: " + pervAd.getNetworkName() + "\n\n"
	// + pervAd.getContent(), Toast.LENGTH_LONG).show();
	// if (!pervAd.isSeen() && pervAdsService != null) {
	// try {
	// pervAdsService.pervAdSeen(pervAd.getId());
	// listAdapter.update();
	// } catch (RemoteException ex) {
	// if (Logger.E)
	// L
	// .e(
	// "an error occurred while calling pervAdSeen method on service",
	// ex);
	// }
	// }
	// }
	// };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set content view
		setContentView(R.layout.pervads_list_activity);

		// create progress dialog
		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);
		progressDialog.setTitle("PervAds");
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

		// init update button
		updateButton = (Button) findViewById(R.id.update_button);
		updateButton.setOnClickListener(updateButtonClickListener);

		resultAdapter = new QueryResultAdapter(this);
		setListAdapter(resultAdapter);

		setControlsEnabled(false);
		setUpdating(false);

		// ensure that service is running
		ensureServiceRunning();

		// bind to service
		if (Logger.V)
			L.v("binding to service");
		bindService(new Intent(this, PervAdsService.class), connection, 0);
	}

	private void ensureServiceRunning() {
		if (!Utils.checkServiceRunning(this, PervAdsService.class)) {
			if (Logger.D)
				L
						.d("service is not running, starting it with IGNORE_AUTO_UPDATE_EXTRA = true");
			// we start the service specifying not to auto-start an update,
			// because we will manage this
			// manually via binding
			startService(new Intent(this, PervAdsService.class).putExtra(
					PervAdsService.IGNORE_AUTO_UPDATE_EXTRA, true));
		}
	}

	private void startUpdate() {
		setControlsEnabled(false);
		setUpdating(true);
		new Thread(updateStarter).start();
	}

	private void setUpdating(boolean updating) {
		updateButton
				.setText(updating ? getString(R.string.pervads_updating_text)
						: getString(R.string.pervads_update_text));
	}

	private void setControlsEnabled(boolean enabled) {
		updateButton.setEnabled(enabled);
		getListView().setEnabled(enabled);
	}

	private void updateStarted() {
		progressDialog.setMessage("update started");
		progressDialog.setIndeterminate(true);
		progressDialog.show();
	}

	private void updateCompleted() {
		// update adapter
		resultAdapter.update();

		setUpdating(false);
		setControlsEnabled(true);

		progressDialog.hide();
	}

	private void updateFailed() {
		// TODO notify user that update has failed
		if (Logger.E)
			L.e("update failed");

		setUpdating(false);
		setControlsEnabled(true);

		progressDialog.hide();
	}

	private void networkScanStarted() {
		progressDialog.setMessage("starting network scan");
	}

	private void networkScanCompleted(int numFoundNetworks) {
		progressDialog.setMessage("network scan completed");
		progressDialog.setIndeterminate(false);
		progressDialog.setProgress(0);
		progressDialog.setMax(numFoundNetworks);
	}

	private void networkScanFailed() {
		// do nothing
	}

	private void networkConnectionStarted(String SSID) {
		progressDialog.setMessage("connecting to network " + SSID);
	}

	private void networkConnectionCompleted(String SSID) {
		incrementProgress();
	}

	private void networkConnectionFailed(String SSID) {
		incrementProgress();
	}

	private void cachedDataCount(int count) {
		progressDialog.setMessage("processing cached data");
		progressDialog.setProgress(0);
		progressDialog.setMax(count);
	}

	private void cachedDataProcessingStarted() {
		// do nothing
	}

	private void cachedDataProcessingCompleted() {
		incrementProgress();
	}

	private void cachedDataProcessingFailed() {
		incrementProgress();
	}

	private void incrementProgress() {
		progressDialog.incrementProgressBy(1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (super.onCreateOptionsMenu(menu)) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.pervads_list_options, menu);
			// menu.add(0, MENU_SETTINGS, 0, "Settings").setIcon(
			// android.R.drawable.ic_menu_preferences);
			return true;
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (!super.onOptionsItemSelected(item)) {
			switch (item.getItemId()) {
				case R.id.settings:
					startActivity(new Intent(this, SettingsActivity.class));
					return true;
				case R.id.queries:
					startActivity(new Intent(this, QueryManagerActivity.class));
					return true;
			}
			return false;
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		if (Logger.V)
			L.v("onDestroy()");

		// dismiss progress dialog
		progressDialog.dismiss();

		// unregister service listener
		if (pervAdsService != null) {
			if (Logger.V)
				L.v("unregistering service listener");
			try {
				pervAdsService.unregisterListener(serviceListener);
			} catch (RemoteException ex) {
				if (Logger.W)
					L.w("cannot unregister service listener", ex);
			}
		}

		// unbind from service
		unbindService(connection);

		super.onDestroy();
	}

}
