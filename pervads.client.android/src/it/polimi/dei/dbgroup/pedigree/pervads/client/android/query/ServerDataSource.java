package it.polimi.dei.dbgroup.pedigree.pervads.client.android.query;

import java.io.InputStream;

public interface ServerDataSource {
	public InputStream getStream();
	public String getKey();
}
