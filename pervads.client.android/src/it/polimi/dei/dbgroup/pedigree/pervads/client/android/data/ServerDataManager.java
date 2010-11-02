package it.polimi.dei.dbgroup.pedigree.pervads.client.android.data;

import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Logger;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import android.content.Context;

public class ServerDataManager {
	private static class ServerDataSourceImpl implements ServerDataSource {
		private InputStream stream;
		private String key;

		private ServerDataSourceImpl(InputStream stream, String key) {
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

		public static ServerDataSource fromFile(File dataFile, String key) {
			try {
				return new ServerDataSourceImpl(new GZIPInputStream(
						new FileInputStream(dataFile)), key);
			} catch (IOException ex) {
				throw new RuntimeException("cannot read data file "
						+ dataFile.getAbsolutePath() + " for key " + key, ex);
			}
		}

	}

	private static final String DATA_FOLDER_NAME = "server_data";
	private static final String DATA_FILE_EXT = ".gz";
	private Context context;
	private final Logger L = new Logger(ServerDataManager.class);
	private boolean updating = false;
	private Set<File> unmodifiedDataFiles = null;

	public ServerDataManager(Context context) {
		this.context = context;
	}

	public void startUpdate() {
		if (updating)
			throw new RuntimeException("already updating");
		updating = true;
		// move all the data files to the update folder
		moveDataToUpdateFolder();
		// initialize the unmodified set
		unmodifiedDataFiles = new HashSet<File>(Arrays
				.asList(getDataUpdateFolder().listFiles()));
	}

	public void endUpdate() {
		if (!updating)
			throw new RuntimeException("not updating");
		updating = false;
		
		// clear unmodified set
		unmodifiedDataFiles = null;
		
		// move all the data files to the cache folder
		moveDataToCacheFolder();
	}
	
	public boolean isUpdating() {
		return updating;
	}

	public ServerDataSource addData(String key, InputStream data) {
		if (!updating)
			throw new IllegalStateException(
					"must be updating in order to add new datas");
		if (Logger.V)
			L.v("adding data for key " + key);

		File dataFile = getDataFileFromKey(key);

		// if data file exists, replace it
		if (dataFile.exists()) {
			if (!dataFile.delete())
				throw new RuntimeException("Cannot delete data file "
						+ dataFile.getAbsolutePath());

			// remove it from the unmodified files
			unmodifiedDataFiles.remove(dataFile);
		}

		try {
			Utils.toFile(data, dataFile, true);
		} catch (IOException ex) {
			throw new RuntimeException("Cannot write to data file "
					+ dataFile.getAbsolutePath(), ex);
		}

		return ServerDataSourceImpl.fromFile(dataFile, key);
	}

	public boolean removeData(String key) {
		if (!updating)
			throw new IllegalStateException(
					"must be updating in order to remove data");

		File dataFile = getDataFileFromKey(key);
		if (dataFile.exists()) {
			if (!dataFile.delete())
				throw new RuntimeException("cannot remove data file "
						+ dataFile.getAbsolutePath() + " for key " + key);
			unmodifiedDataFiles.remove(dataFile);
			return true;
		}

		return false;
	}
	
	public int countUnmodifiedData() {
		if (!updating)
			throw new RuntimeException(
					"Must be updating in order to count unmodified data");
		
		return unmodifiedDataFiles.size();
	}

	public Iterator<ServerDataSource> getUnmodifiedData() {
		if (!updating)
			throw new RuntimeException(
					"Must be updating in order to retrieve data");

		return new Iterator<ServerDataSource>() {
			private Iterator<File> unmodifiedDataIterator = unmodifiedDataFiles
					.iterator();
			private File curr = null;
			private boolean removed = false;

			@Override
			public boolean hasNext() {
				return unmodifiedDataIterator.hasNext();
			}

			@Override
			public ServerDataSource next() {
				if (!hasNext())
					throw new NoSuchElementException();
				removed = false;
				curr = unmodifiedDataIterator.next();

				return ServerDataSourceImpl.fromFile(curr,
						getKeyFromDataFile(curr));
			}

			@Override
			public void remove() {
				if (removed || curr == null)
					throw new NoSuchElementException();
				if (!curr.delete())
					throw new RuntimeException(
							"cannot delete server data file "
									+ curr.getAbsolutePath());
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

	private File getDataFileFromKey(String key) {
		String dataFileName = key + DATA_FILE_EXT;
		return new File(getDataUpdateFolder(), dataFileName);
	}

	private String getKeyFromDataFile(File dataFile) {
		return dataFile.getName().substring(0,
				dataFile.getName().length() - DATA_FILE_EXT.length());
	}
}
