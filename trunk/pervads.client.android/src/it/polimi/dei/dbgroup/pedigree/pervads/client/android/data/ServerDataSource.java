package it.polimi.dei.dbgroup.pedigree.pervads.client.android.data;

import java.io.InputStream;

public interface ServerDataSource {
	public InputStream getStream();
	public String getKey();
}
