package it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi;

public interface IWifiAdapterListener {
	public void scanCompleted();
	public void scanFailed(WifiAdapterException ex);
	public void connectionCompleted();
	public void connectionFailed(WifiAdapterException ex);
}
