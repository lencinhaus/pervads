package it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi;

public interface IWifiAdapter {
	public void initialize() throws WifiAdapterException;
	
	public boolean isSupported() throws WifiAdapterException;
	
	public boolean isEnabled() throws WifiAdapterException;
	
	public void startSession() throws WifiAdapterException;
	
	public void endSession() throws WifiAdapterException;
	
	public boolean isSessionStarted() throws WifiAdapterException;
	
	public void startNetworkScan() throws WifiAdapterException;
	
	public WifiNetwork[] getReachableNetworks() throws WifiAdapterException;
	
	public void connect(WifiNetwork network) throws WifiAdapterException;
	
	public boolean isConnected() throws WifiAdapterException;
	
	public WifiNetwork getConnectedNetwork() throws WifiAdapterException;
	
	public ConnectionInfo getConnectionInfo() throws WifiAdapterException;
}
