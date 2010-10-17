package it.polimi.dei.dbgroup.pedigree.pervads.wifi;

public class ConnectionInfo {
	//public static final int ADDRESS_NOT_SET = 0; //TODO can be used?
	
	private int dns1;
	private int dns2;
	private int gateway;
	private int ipAddress;
	private int leaseDuration;
	private int netmask;
	private int serverAddress;
	private int linkSpeed;
	private String macAddress;
	
	public ConnectionInfo()
	{
		
	}

	public int getDns1() {
		return dns1;
	}

	public void setDns1(int dns1) {
		this.dns1 = dns1;
	}

	public int getDns2() {
		return dns2;
	}

	public void setDns2(int dns2) {
		this.dns2 = dns2;
	}

	public int getGateway() {
		return gateway;
	}

	public void setGateway(int gateway) {
		this.gateway = gateway;
	}

	public int getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(int ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getLeaseDuration() {
		return leaseDuration;
	}

	public void setLeaseDuration(int leaseDuration) {
		this.leaseDuration = leaseDuration;
	}

	public int getNetmask() {
		return netmask;
	}

	public void setNetmask(int netmask) {
		this.netmask = netmask;
	}

	public int getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(int serverAddress) {
		this.serverAddress = serverAddress;
	}

	public int getLinkSpeed() {
		return linkSpeed;
	}

	public void setLinkSpeed(int linkSpeed) {
		this.linkSpeed = linkSpeed;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	
	
}
