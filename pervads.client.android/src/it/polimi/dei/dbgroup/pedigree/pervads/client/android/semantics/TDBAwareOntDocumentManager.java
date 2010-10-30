package it.polimi.dei.dbgroup.pedigree.pervads.client.android.semantics;

import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Logger;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.rdf.model.Model;

public class TDBAwareOntDocumentManager extends OntDocumentManager {
	private static TDBAwareOntDocumentManager instance = null;
	private final Logger L = new Logger("TDBDocManager");

	private TDBAwareOntDocumentManager() {
		super();
	}

	public static TDBAwareOntDocumentManager getInstance() {
		if (instance == null)
			instance = new TDBAwareOntDocumentManager();
		return instance;
	}

	@Override
	public Model getModel(String uri) {
		Model m = super.getModel(uri);
		if (m == null) {
			TDBModelManager tdbManager = TDBModelManager.getInstance();
			if (tdbManager.hasStoreModel(uri)) {
				if (Logger.D)
					L.d("Loading TDB model for uri " + uri);
				m = tdbManager.getStoreModel(uri);
				if (m != null && getFileManager().getCachingModels()) {
					if (Logger.D)
						L.d("Caching TDB model for uri " + uri);
					getFileManager().addCacheModel(uri, m);
				}
			}
		}
		return m;
	}
}
