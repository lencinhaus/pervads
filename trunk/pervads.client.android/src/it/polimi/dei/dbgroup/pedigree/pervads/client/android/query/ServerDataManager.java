package it.polimi.dei.dbgroup.pedigree.pervads.client.android.query;

import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Logger;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;

import android.content.Context;

public class ServerDataManager {
	private class ServerDataSourceImpl implements ServerDataSource {
		private InputStream stream;
		private String key;

		public ServerDataSourceImpl(InputStream stream, String key) {
			super();
			this.stream = stream;
			this.key = key;
		}

		@Override
		public String getKey() {
			return key;
		}

		@Override
		public InputStream getStream() {
			return stream;
		}

	}
	
	private static final String DATA_FOLDER_NAME = "server_data";
	private static final String DATA_FILE_EXT = ".gz";
	private Context context;
	private final Logger L = new Logger(ServerDataManager.class);
	private boolean updating = false;

	public ServerDataManager(Context context) {
		this.context = context;
	}

	public void startUpdate() {
		if (updating)
			throw new RuntimeException("already updating");
		updating = true;
		// move all the data files to the update folder
		moveDataToUpdateFolder();
	}

	public void endUpdate() {
		if (!updating)
			throw new RuntimeException("not updating");
		updating = false;
		// move all the data files to the cache folder
		moveDataToCacheFolder();
	}

	public boolean addData(String key, String data) {
		if (!updating)
			throw new RuntimeException(
					"Must be updating in order to add new data");
		if(Logger.V) L.v("adding data for key " + key);
		String dataFileName = key + DATA_FILE_EXT;
		File dataFile = new File(getDataUpdateFolder(), dataFileName);

		// if data file exists, replace it
		boolean replaced = false;
		if (dataFile.exists()) {
			if (!dataFile.delete())
				throw new RuntimeException("Cannot delete data file "
						+ dataFile.getAbsolutePath());
			replaced = true;
		}

		try {
			Utils.toFile(data, dataFile, true);
		} catch (IOException ex) {
			throw new RuntimeException("Cannot write to data file "
					+ dataFile.getAbsolutePath(), ex);
		}

		return replaced;
	}
	
	public int countData() {
		return getDataUpdateFolder().list().length;
	}

	public Iterator<ServerDataSource> getData() {
		if (!updating)
			throw new RuntimeException(
					"Must be updating in order to retrieve data");

		return new Iterator<ServerDataSource>() {
			private File[] dataFiles = getDataUpdateFolder().listFiles();
			private int index = -1;
			private boolean removed = false;

			@Override
			public boolean hasNext() {
				return index < dataFiles.length - 1;
			}

			@Override
			public ServerDataSource next() {
				if (!hasNext())
					throw new NoSuchElementException();
				removed = false;
				File curr = dataFiles[++index];
				String key = curr.getName().substring(0, curr.getName().length() - DATA_FILE_EXT.length());
				try {
					return new ServerDataSourceImpl(new GZIPInputStream(new FileInputStream(curr)), key);
				} catch (IOException ex) {
					throw new RuntimeException("Cannot read server data file "
							+ curr.getAbsolutePath(), ex);
				}
			}

			@Override
			public void remove() {
				if(index == -1 || index >= dataFiles.length) throw new NoSuchElementException();
				if(removed) throw new NoSuchElementException();
				File curr = dataFiles[index];
				if(!curr.delete()) throw new RuntimeException("cannot delete server data file " + curr.getAbsolutePath());
				removed = true;
			}
		};
	}

	private void moveDataToUpdateFolder() {
		Utils.moveAll(getDataCacheFolder(), getDataUpdateFolder(), true, true);
	}

	private void moveDataToCacheFolder() {
		Utils.moveAll(getDataUpdateFolder(), getDataCacheFolder(), true, true);
	}

	private File getDataCacheFolder() {
		return getDataFolder(context.getCacheDir());
	}

	private File getDataUpdateFolder() {
		return getDataFolder(context.getFilesDir());
	}

	private File getDataFolder(File parent) {
		File dataFolder = new File(parent, DATA_FOLDER_NAME);
		if (!dataFolder.exists()) {
			L.i("Creating server data folder " + dataFolder.getAbsolutePath());
			dataFolder.mkdirs();
		}
		return dataFolder;
	}
}
