package it.polimi.dei.dbgroup.pedigree.pervads.client.android.util;

import java.io.File;
import java.io.IOException;

import android.content.Context;

import com.thoughtworks.xstream.XStream;

public abstract class SerializedDataManager<T> {
	private T data = null;
	private Context context;
	private XStream xstream = null;
	private final Logger L = new Logger(getClass());

	public SerializedDataManager(Context context) {
		this.context = context;
	}

	protected T getData() {
		synchronized (this) {
			if (data == null) {
				deserializeData();
			}
		}
		return data;
	}

	protected void setData(T data) {
		synchronized (this) {
			this.data = data;
		}
	}

	protected void writeData() {
		synchronized (this) {
			serializeData();
		}
	}

	public void synchronize() {
		synchronized (this) {
			data = null;
		}
	}
	
	public void clear() {
		synchronized(this) {
			data = getInitialData();
			serializeData();
		}
	}

	private void deserializeData() {
		File dataFile = getDataFile();
		if (!dataFile.exists())
			data = getInitialData();
		else {
			try {
				String dataXml = Utils.toString(dataFile, true);
				if (Logger.V)
					L.v("deserialized " + getName() + "data: " + dataXml);
				data = (T) getXStream().fromXML(dataXml);
			} catch (IOException ex) {
				throw new RuntimeException("cannot deserialize data from "
						+ dataFile.getAbsolutePath(), ex);
			}
		}
	}

	private void serializeData() {
		File dataFile = getDataFile();
		if (dataFile.exists() && !dataFile.delete())
			throw new RuntimeException("cannot delete data file "
					+ dataFile.getAbsolutePath());
		if (data != null) {
			String dataXml = getXStream().toXML(data);
			if (Logger.V)
				L.v("serialized " + getName() + "data: " + dataXml);
			try {
				Utils.toFile(dataXml, dataFile, true);
			} catch (IOException ex) {
				throw new RuntimeException("cannot serialize data to "
						+ dataFile.getAbsolutePath(), ex);
			}
		}
	}

	private File getDataFile() {
		String dataFileName = getName() + ".xml.gz";
		return context.getFileStreamPath(dataFileName);
	}

	protected abstract String getName();

	protected abstract T getInitialData();

	private XStream getXStream() {
		if (xstream == null) {
			xstream = new XStream();
			initializeXStream(xstream);
		}
		return xstream;
	}

	protected void initializeXStream(XStream xstream) {

	}
}
