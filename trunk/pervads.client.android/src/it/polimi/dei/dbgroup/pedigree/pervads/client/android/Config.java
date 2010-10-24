package it.polimi.dei.dbgroup.pedigree.pervads.client.android;

import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Utils;

public final class Config {
	public static final boolean LOG = true;
	public static final boolean LOGD = true;
	public static final boolean LOGV = true;
	
	/**
	 * If true, context store and index are rebuilt at every activity launch
	 */
	public static final boolean DEBUG_RESET = false;
	
	public static final boolean TESTING_BEHAVIOR = true;
	public static final String TESTING_SSID = "LOREPRI";
	public static final int TESTING_GATEWAY = Utils.parseIPAddress("192.168.1.101");
	
	public static final String WEP40_PASSWORD = "1234567890";
	public static final String WEP104_PASSWORD = "12345678901234567890123456";
	public static final String WPA_PSK_PASSWORD = "noneveroanchetu";
}
