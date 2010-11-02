package it.polimi.dei.dbgroup.pedigree.pervads.client.android.data;

import java.io.IOException;

import android.net.Uri;

public abstract class AttachedMediaManager {
	public static final AttachedMediaManager Offline = new AttachedMediaManager() {

		@Override
		public String cacheIt(String path) {
			// always return null because it's offline and cannot download the
			// media
			return null;
		}
	};

	/**
	 * This method returns:
	 * - the unchanged path if it's an absolute URL (i.e. can still be downloaded after we disconnect from this hotspot)
	 * - the absolute path of the locally cached media if path was a relative URI (the media is downloaded and saved in external or internal storage)
	 * - null if the path was relative but if was unreachable
	 * @param path the possibly relative path of this attached media
	 * @return
	 * @throws IOException in case of error during media download
	 */
	public String cacheIfNeeded(String path) throws IOException {
		Uri uri = Uri.parse(path);
		// if uri is absolute, return it because it doesn't need to be cached
		// now
		if (uri.isAbsolute())
			return path;
		// try to download this uri
		return cacheIt(path);
	}

	protected abstract String cacheIt(String relativePath) throws IOException;
}
