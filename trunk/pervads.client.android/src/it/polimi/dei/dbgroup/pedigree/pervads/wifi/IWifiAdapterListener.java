package it.polimi.dei.dbgroup.pedigree.pervads.wifi;

public interface IWifiAdapterListener {
	public void scanCompleted();
	public void scanFailed(WifiAdapterException ex);
	public void connectionCompleted();
	public void connectionFailed(WifiAdapterException ex);
	public void disconnectionCompleted();
	public void disconnectionFailed(WifiAdapterException ex);
}
