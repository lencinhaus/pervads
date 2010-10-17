package it.polimi.dei.dbgroup.pedigree.pervads;

import it.polimi.dei.dbgroup.pedigree.pervads.preference.Settings;
import it.polimi.dei.dbgroup.pedigree.pervads.util.Logger;
import it.polimi.dei.dbgroup.pedigree.pervads.util.Utils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LifeCycleEventReceiver extends BroadcastReceiver {
	private static final Logger L = new Logger(LifeCycleEventReceiver.class
			.getSimpleName());

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Logger.V)
			L.v("onReceive(" + context.getPackageName() + ", " + intent + ")");
		final String action = intent.getAction();

		final Settings settings = new Settings(context);

		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			/*
			 * if auto-update is enabled, we have to start pervads service at
			 * boot
			 */

			// read auto-update setting
			boolean autoUpdate = settings.getAutoUpdate();
			boolean serviceRunning = Utils.checkServiceRunning(context,
					PervAdsService.class);
			
			if(Logger.D) L.d("boot completed. auto-update: " + autoUpdate + ", serviceRunning: " + serviceRunning);
			// if auto-update is enabled and service is not already started,
			// start it, bind to it and enable autoupdating
			if (autoUpdate && !serviceRunning) {
				if (Logger.I)
					L
							.i("starting PervAdsService at boot because auto-update is enabled");
				final Intent serviceIntent = getServiceIntent(context);
				context.startService(serviceIntent);
			}
		} /*else if (Intent.ACTION_SHUTDOWN.equals(action)) {
			// stop PervAdsService if it's running
			if (Utils.checkServiceRunning(context, PervAdsService.class)) {
				context.stopService(getServiceIntent(context));
			}
		}*/
	}

	private static final Intent getServiceIntent(Context context) {
		return new Intent(context, PervAdsService.class);
	}

}
