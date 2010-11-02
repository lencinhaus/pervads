package it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi;

import java.util.HashMap;
import java.util.Map;

public class WifiNetwork {
	private String SSID;
	private String BSSID;
	private WifiSecurity security;
	private int signalLevel;
	private String password;
	private Map<String, String> extendedProperties = new HashMap<String, String>();
	
	public static final int SIGNAL_LEVEL_UNKNOWN = -1;

	public WifiNetwork() {

	}

	public String getSSID() {
		return SSID;
	}

	public void setSSID(String sSID) {
		SSID = sSID;
	}

	public String getBSSID() {
		return BSSID;
	}

	public void setBSSID(String bSSID) {
		BSSID = bSSID;
	}

	public WifiSecurity getSecurity() {
		return security;
	}

	public void setSecurity(WifiSecurity security) {
		this.security = security;
	}

	public int getSignalLevel() {
		return signalLevel;
	}

	public void setSignalLevel(int signalLevel) {
		this.signalLevel = signalLevel;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Map<String, String> getExtendedProperties() {
		return extendedProperties;
	}

	@Override
	public String toString() {
		//TODO more meaningful output here
		return SSID + " (" + BSSID + ")";
	}
	
	
}
