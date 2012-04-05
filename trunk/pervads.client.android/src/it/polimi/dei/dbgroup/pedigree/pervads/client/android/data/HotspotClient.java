package it.polimi.dei.dbgroup.pedigree.pervads.client.android.data;

import it.polimi.dei.dbgroup.pedigree.pervads.client.android.Config;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Logger;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Utils;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi.ConnectionInfo;

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class HotspotClient {
	private String gatewayIP;
	private static final HttpParams params = new BasicHttpParams();
	static {
		HttpConnectionParams.setConnectionTimeout(params, 5000);
		HttpConnectionParams.setSoTimeout(params, 5000);
	}
	private final HttpClient client = new DefaultHttpClient(params);
	private final Logger L = new Logger(HotspotClient.class);

	public HotspotClient(int gateway) {
		this.gatewayIP = Utils.formatIPAddress(gateway);
	}

	public InputStream download(String path) {
		String uri = "http://" + gatewayIP + "/" + path;

		if (Logger.D)
			L.d("downloading from URI " + uri);
		try {
			HttpGet request = new HttpGet(uri);
			HttpResponse response = client.execute(request);
			if (response.getStatusLine() == null) {
				if (Logger.W)
					L
							.w("hotspot returned a response with no status line while trying to download from URI "
									+ uri);
			} else {
				int status = response.getStatusLine().getStatusCode();
				if (status != HttpStatus.SC_OK) {
					if (Logger.I)
						L.i("hotspot returned non-OK status (" + status
								+ ") while trying to download from URI " + uri);
				} else {
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						return entity.getContent();
					}
				}
			}
		} catch (Exception ex) {
			if (Logger.W)
				L.w("an error occurred while downloading from URI " + uri, ex);
		}

		return null;
	}

	public static HotspotClient createFromConnection(ConnectionInfo connection) {
		int gateway = 0;

		if (Config.TESTING_BEHAVIOR) {
			gateway = Config.TESTING_GATEWAY;
		} else {
			gateway = connection.getGateway();
		}

		return new HotspotClient(gateway);
	}
}
