package it.polimi.dei.dbgroup.pedigree.pervads.wifi;

public class WifiAdapterException extends Exception {
	public static final long serialVersionUID = 0L;
	private WifiNetwork network = null;
	
	public WifiAdapterException(String message)
	{
		super(message);
	}
	
	public WifiAdapterException(String message, WifiNetwork network)
	{
		super(message);
		this.network = network;
	}
	
	public WifiAdapterException(String message, Throwable throwable)
	{
		super(message, throwable);
	}
	
	public WifiAdapterException(String message, Throwable throwable, WifiNetwork network)
	{
		super(message, throwable);
		this.network = network;
	}

	public WifiNetwork getNetwork() {
		return network;
	}
}
