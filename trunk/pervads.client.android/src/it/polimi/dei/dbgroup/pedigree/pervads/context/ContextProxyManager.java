package it.polimi.dei.dbgroup.pedigree.pervads.context;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextModelFactory;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextModelProxy;
import it.polimi.dei.dbgroup.pedigree.contextmodel.vocabulary.ContextModel;
import it.polimi.dei.dbgroup.pedigree.pervads.Config;
import it.polimi.dei.dbgroup.pedigree.pervads.R;
import it.polimi.dei.dbgroup.pedigree.pervads.util.Initializable;
import it.polimi.dei.dbgroup.pedigree.pervads.util.Logger;
import it.polimi.dei.dbgroup.pedigree.pervads.util.ProgressMonitor;
import it.polimi.dei.dbgroup.pedigree.pervads.util.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.TDBLoader;
import com.hp.hpl.jena.tdb.store.GraphTDB;

public class ContextProxyManager implements Initializable {
	public static final String CONTEXT_MODEL_ASSET = "owl/pervads.owl";
	public static final String CONTEXT_STORE_ARCHIVE_ASSET = "pervads_tdb.zip";
	private static final String STORE_DIRECTORY_NAME = "store";
	// public static final String CONTEXT_MODEL_ASSET = "owl/dummy.owl";
	// public static final String CONTEXT_STORE_ARCHIVE_ASSET = "dummy_tdb.zip";
	private static ContextProxyManager instance = null;
	private final Logger L = new Logger(ContextProxyManager.class
			.getSimpleName());
	private ContextModelProxy proxy = null;
	private boolean initialized = false;

	private ContextProxyManager() {
		
	}

	public ContextModelProxy getProxy() {
		return proxy;
	}

	public static ContextProxyManager getInstance() {
		if (instance == null)
			instance = new ContextProxyManager();
		return instance;
	}

	@Override
	public void initialize(Context context, ProgressMonitor monitor) {
		if (!isInitialized(context)) {
			monitor.indeterminate(true);
			monitor.message(R.string.context_model_manager_operation_init);
			File storeDir = getStoreDirectory(context);

			if (!storeDir.exists()) {
				// first try to uncompress an archived tdb store
				try {
					L.d("trying to uncompress tdb store");
					InputStream is = context.getAssets().open(
							CONTEXT_STORE_ARCHIVE_ASSET);
					Utils.unzip(is, storeDir);
					is.close();
					L.d("tdb store uncompressed");
				} catch (IOException ex) {
					try {
						// no archived tdb store: create one bulk loading rdf
						// triples
						L
								.d("cannot uncompress tdb store, bulk loading tdb model");
						Model tdbModel = TDBFactory
								.createModel(getStoreDirectory(context)
										.getAbsolutePath());
						TDBLoader.load((GraphTDB) tdbModel.getGraph(),
								"assets/" + CONTEXT_MODEL_ASSET, Config.LOGD);
						tdbModel.close();
						TDB.closedown();
						L.d("tdb model bulk loaded");
					} catch (Exception ex2) {
						throw new RuntimeException("Cannot read context model",
								ex2);
					}
				}
			}

			proxy = createProxy(context);
		}
	}

	@Override
	public boolean isInitialized(Context context) {
		if (Config.DEBUG_RESET) {
			if (!initialized) {
				Utils.recursiveDelete(getStoreDirectory(context));
				initialized = true;
			}
		}

		return proxy != null;
	}

	private File getStoreDirectory(Context context) {
		return context.getFileStreamPath(STORE_DIRECTORY_NAME);
	}

	private ContextModelProxy createProxy(Context context) {
		OntDocumentManager.getInstance().addAltEntry(ContextModel.URI,
				ContextModelFactory.CONTEXT_MODEL_ALT_URI);
		Model tdbModel = TDBFactory.createModel(getStoreDirectory(context)
				.getAbsolutePath());
		OntModel ontModel = ModelFactory.createOntologyModel(
				OntModelSpec.OWL_DL_MEM, tdbModel);
		return ContextModelFactory.createProxy(ontModel);
	}
}
