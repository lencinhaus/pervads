package it.polimi.dei.dbgroup.pedigree.pervads.client.android.wifi.windows;

public class ServerSideException extends Exception {
	public static final long serialVersionUID = 0L;

	public ServerSideException() {
		super();
	}

	public ServerSideException(String message) {
		super(message);
	}

	public ServerSideException(ServerSideException inner) {
		super(inner);
	}

	public ServerSideException(String message, ServerSideException inner) {
		super(message, inner);
	}
}
