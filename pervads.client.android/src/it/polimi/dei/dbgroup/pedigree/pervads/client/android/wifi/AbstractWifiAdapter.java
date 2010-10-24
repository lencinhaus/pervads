package it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi;


public abstract class AbstractWifiAdapter implements IWifiAdapter {
	private IWifiAdapterListener listener;
	
	public AbstractWifiAdapter(IWifiAdapterListener listener)
	{
		if(listener == null) throw new NullPointerException("listener cannot be null");
		this.listener = listener;
	}
	
	protected IWifiAdapterListener getListener()
	{
		return listener;
	}

	@Override
	public abstract void initialize() throws WifiAdapterException;

	@Override
	public abstract boolean isSupported() throws WifiAdapterException;

	@Override
	public abstract boolean isEnabled() throws WifiAdapterException;

	@Override
	public abstract void connect(WifiNetwork network) throws WifiAdapterException;

	@Override
	public abstract void disconnect() throws WifiAdapterException;

	@Override
	public abstract WifiNetwork[] getReachableNetworks() throws WifiAdapterException;

	@Override
	public abstract WifiNetwork getConnectedNetwork() throws WifiAdapterException;

	@Override
	public abstract boolean isConnected() throws WifiAdapterException;

	@Override
	public abstract void startNetworkScan() throws WifiAdapterException;

	@Override
	public abstract void endSession() throws WifiAdapterException;

	@Override
	public abstract void startSession() throws WifiAdapterException;
	
	@Override
	public abstract ConnectionInfo getConnectionInfo() throws WifiAdapterException;
}
