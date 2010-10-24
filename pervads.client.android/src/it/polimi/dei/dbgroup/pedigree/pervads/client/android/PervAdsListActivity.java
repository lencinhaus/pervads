package it.polimi.dei.dbgroup.pedigree.pervads.client.android;

import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Logger;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Utils;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.IPervAdsService;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.IPervAdsServiceListener;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.R;
import android.app.Activity;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class PervAdsListActivity extends Activity {
	private static final Logger L = new Logger(PervAdsListActivity.class
			.getSimpleName());

	private static final int UPDATE_STARTED_MESSAGE = 1;
	private static final int UPDATE_COMPLETED_MESSAGE = 2;
	private static final int UPDATE_FAILED_MESSAGE = 3;
	private static final int NETWORK_SCAN_STARTED = 4;
	private static final int NETWORK_SCAN_COMPLETED = 5;
	private static final int NETWORK_SCAN_FAILED = 6;
	private static final int NETWORK_CONNECTION_STARTED = 7;
	private static final int NETWORK_CONNECTION_COMPLETED = 8;
	private static final int NETWORK_CONNECTION_FAILED = 9;
	private static final String NETWORK_SSID_KEY = "SSID";
	private static final String NUM_FOUND_NETWORKS_KEY = "numFoundNetworks";

	private ListView pervAdsList;
	private PervAdsAdapter listAdapter;
	private Button updateButton;
	private ProgressDialog progressDialog = null;
	private IPervAdsService pervAdsService = null;
	private int numFoundNetworks = 0;
	private int numConnectedNetworks = 0;

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
				case NETWORK_SCAN_STARTED:
					networkScanStarted();
					break;
				case NETWORK_SCAN_COMPLETED:
					networkScanCompleted(msg.getData().getInt(
							NUM_FOUND_NETWORKS_KEY));
					break;
				case NETWORK_SCAN_FAILED:
					networkScanFailed();
					break;
				case NETWORK_CONNECTION_STARTED:
				case NETWORK_CONNECTION_COMPLETED:
				case NETWORK_CONNECTION_FAILED:
					String SSID = msg.getData().getString(NETWORK_SSID_KEY);
					switch (msg.what) {
						case NETWORK_CONNECTION_STARTED:
							networkConnectionStarted(SSID);
							break;
						case NETWORK_CONNECTION_COMPLETED:
							networkConnectionCompleted(SSID);
							break;
						case NETWORK_CONNECTION_FAILED:
							networkConnectionFailed(SSID);
							break;
					}
					break;
				default:
					super.handleMessage(msg);
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

			// update adapter service
			listAdapter.setService(null);
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

						// set adapter service
						listAdapter.setService(pervAdsService);

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
							// just refresh the list
							listAdapter.update();

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

			Message message = Message.obtain(handler);
			message.getData().putString(NETWORK_SSID_KEY, SSID);
			switch (type) {
				case EventTypes.Started:
					message.what = NETWORK_CONNECTION_STARTED;
					break;
				case EventTypes.Completed:
					message.what = NETWORK_CONNECTION_COMPLETED;
					break;
				case EventTypes.Failed:
					message.what = NETWORK_CONNECTION_FAILED;
					break;
				default:
					return;
			}
			message.sendToTarget();
		}

		@Override
		public void networkScanEvent(int type, int numFoundNetworks)
				throws RemoteException {
			if (Logger.V)
				L.v("networkScanEvent(" + EventTypes.describe(type) + ")");

			Message message = Message.obtain(handler);
			message.getData().putInt(NUM_FOUND_NETWORKS_KEY, numFoundNetworks);

			switch (type) {
				case EventTypes.Started:
					message.what = NETWORK_SCAN_STARTED;
					break;
				case EventTypes.Completed:
					message.what = NETWORK_SCAN_COMPLETED;
					break;
				case EventTypes.Failed:
					message.what = NETWORK_SCAN_FAILED;
					break;
			}

			message.sendToTarget();
		}

	};

	private final OnClickListener updateButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			startUpdate();
		}
	};

	private final OnItemClickListener pervAdClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			PervAd pervAd = listAdapter.getPervAd(position);
			Toast.makeText(
					PervAdsListActivity.this,
					"Network: " + pervAd.getNetworkName() + "\n\n"
							+ pervAd.getContent(), Toast.LENGTH_LONG).show();
			if (!pervAd.isSeen() && pervAdsService != null) {
				try {
					pervAdsService.pervAdSeen(pervAd.getId());
					listAdapter.update();
				} catch (RemoteException ex) {
					if (Logger.E)
						L
								.e(
										"an error occurred while calling pervAdSeen method on service",
										ex);
				}
			}
		}
	};

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

		// init pervads list
		pervAdsList = (ListView) findViewById(R.id.pervads_list);
		pervAdsList.setOnItemClickListener(pervAdClickListener);

		listAdapter = new PervAdsAdapter(this);
		pervAdsList.setAdapter(listAdapter);

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
		pervAdsList.setEnabled(enabled);
	}

	private void updateStarted() {
		progressDialog.setMessage("update started");
		progressDialog.setIndeterminate(true);
		progressDialog.show();
	}

	private void updateCompleted() {
		// update adapter
		listAdapter.update();

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
		// progressDialog.setMessage("network scan completed");
		this.numFoundNetworks = numFoundNetworks;
		numConnectedNetworks = 0;
		progressDialog.setIndeterminate(false);
		progressDialog.setProgress(0);
		progressDialog.setMax(numFoundNetworks);
	}

	private void networkScanFailed() {
		// progressDialog.setMessage("network scan failed");
	}

	private void networkConnectionStarted(String SSID) {
		progressDialog.setMessage("connecting to network " + SSID);
	}

	private void networkConnectionCompleted(String SSID) {
		// progressDialog.setMessage("connection to network " + SSID +
		// " completed");
		updateConnectionProgress();
	}

	private void networkConnectionFailed(String SSID) {
		// progressDialog.setMessage("connection to network " + SSID +
		// " failed");
		updateConnectionProgress();
	}

	private void updateConnectionProgress() {
		numConnectedNetworks++;
		// int progressPercent = Math.max(0, Math.min(100, (int)
		// Math.round(((double) numConnectedNetworks / (double)
		// numFoundNetworks)*100D)));
		// progressDialog.setProgress(progressPercent);
		progressDialog.setProgress(numConnectedNetworks);
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
