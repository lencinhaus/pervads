package it.polimi.dei.dbgroup.pedigree.pervads.client.android.data;

import it.polimi.dei.dbgroup.pedigree.pervads.client.android.Config;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Logger;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.os.Environment;

public class HotspotAttachedMediaManager extends AttachedMediaManager {
	private static final String ATTACHED_MEDIA_CACHE_FOLDER_NAME = "attached_media";
	private final Logger L = new Logger(AttachedMediaManager.class);
	private File attachedMediaCacheFolder = null;
	private HotspotClient client;

	public HotspotAttachedMediaManager(Context context, String key,
			HotspotClient client) {
		this.client = client;
		initAttachedMediaCacheFolder(context, key);
	}

	@Override
	protected String cacheIt(String relativePath) throws IOException {
		if (Logger.D)
			L.d("trying to download attached media " + relativePath);
		InputStream is = client.download(relativePath);
		if (is != null) {
			File cachedFileName = new File(attachedMediaCacheFolder,
					getLocalAttachedMediaFileName(relativePath));
			// TODO handle the fact that it could be already present and up to
			// date
			// we should check the last modified date here
			if (cachedFileName.exists())
				if (!cachedFileName.delete())
					throw new RuntimeException(
							"cannot delete local attached media cache file "
									+ cachedFileName.getAbsolutePath());
			try {
				Utils.toFile(is, cachedFileName);
				if (Logger.D)
					L.d("attached media " + relativePath
							+ " downloaded and saved to file "
							+ cachedFileName.getAbsolutePath());
				return cachedFileName.getAbsolutePath();
			} finally {
				is.close();
			}
		} else {
			if (Logger.W)
				L.w("could not download attached media " + relativePath);
			return null;
		}
	}

	private String getLocalAttachedMediaFileName(String path) {
		return path.replaceAll("/", "_");
	}

	private void initAttachedMediaCacheFolder(Context context, String key) {
		// if external storage is available, then use it
		String relativePath = ATTACHED_MEDIA_CACHE_FOLDER_NAME + "/" + key;
		String externalStorageState = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(externalStorageState)) {
			File externalRootDir = Environment.getExternalStorageDirectory();
			attachedMediaCacheFolder = new File(externalRootDir,
					"Android/data/" + Config.ANDROID_PACKAGE_NAME + "/cache/"
							+ relativePath);
		} else {
			// else, use internal storage
			File internalCacheDir = context.getCacheDir();
			attachedMediaCacheFolder = new File(internalCacheDir, relativePath);
		}

		if (!attachedMediaCacheFolder.exists())
			if (!attachedMediaCacheFolder.mkdirs())
				throw new RuntimeException(
						"cannot create attached media cache folder "
								+ attachedMediaCacheFolder.getAbsolutePath());
	}
}
