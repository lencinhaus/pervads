package it.polimi.dei.dbgroup.pedigree.pervads.client.android.semantics;

import it.polimi.dei.dbgroup.pedigree.pervads.client.android.Config;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Initializable;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Logger;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.ProgressMonitor;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;

public class TDBModelManager implements Initializable {
	private static final String CONTEXT_STORE_ARCHIVE_ASSET = "pervads-context-model_tdb.zip";
	// private static final String CONTEXT_STORE_ARCHIVE_ASSET =
	// "dummy_tdb.zip";
	private static final String STORE_DIRECTORY_NAME = "store";
	private static TDBModelManager instance = null;
	private final Logger L = new Logger(TDBModelManager.class.getSimpleName());
	private Dataset dataset = null;

	private TDBModelManager() {

	}

	public static TDBModelManager getInstance() {
		if (instance == null)
			instance = new TDBModelManager();
		return instance;
	}

	public boolean hasStoreModel(String uri) {
		return dataset.containsNamedModel(uri);
	}

	public Model getStoreModel(String uri) {
		if (!hasStoreModel(uri))
			throw new IllegalArgumentException("no TDB stored model for uri "
					+ uri);
		return dataset.getNamedModel(uri);
	}

	@Override
	public void initialize(Context context, ProgressMonitor monitor) {
		if (!isInitialized(context)) {
			L.d("initializing TDB model manager");
			monitor.indeterminate(true);
			File storeDir = getStoreDirectory(context);

			if (Config.DEBUG_RESET || !storeDir.exists()) {
				if (Config.DEBUG_RESET)
					Utils.recursiveDelete(storeDir);
				// first try to uncompress an archived tdb store
				try {
					L.d("trying to uncompress tdb store");
					InputStream is = context.getAssets().open(
							CONTEXT_STORE_ARCHIVE_ASSET);
					Utils.unzip(is, storeDir);
					is.close();
					L.d("tdb store uncompressed");
				} catch (IOException ex) {
					throw new RuntimeException(
							"cannot uncompress TDB store in "
									+ storeDir.getAbsolutePath(), ex);
				}
			}

			// create dataset
			dataset = TDBFactory.createDataset(storeDir.getAbsolutePath());
		}
	}

	@Override
	public boolean isInitialized(Context context) {
		return dataset != null;
	}

	private File getStoreDirectory(Context context) {
		return context.getFileStreamPath(STORE_DIRECTORY_NAME);
	}
}
