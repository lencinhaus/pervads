package it.polimi.dei.dbgroup.pedigree.pervads.client.android.context;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextModelFactory;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextModelProxy;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.R;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.semantics.TDBAwareOntDocumentManager;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.semantics.TDBModelManager;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Initializable;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Logger;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.ProgressMonitor;
import it.polimi.dei.dbgroup.pedigree.pervads.model.vocabulary.PervADsContextModel;
import android.content.Context;

import com.hp.hpl.jena.rdf.model.Model;

public class ContextProxyManager implements Initializable {
	private static ContextProxyManager instance = null;
	private final Logger L = new Logger(ContextProxyManager.class);
	private ContextModelProxy proxy = null;

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
			L.d("initializing context proxy manager");
			monitor.indeterminate(true);
			monitor.message(R.string.context_model_manager_operation_init);
			TDBModelManager tdbManager = TDBModelManager.getInstance();
			if (!tdbManager.isInitialized(context))
				tdbManager.initialize(context, monitor);
			proxy = createProxy(context);
		}
	}

	@Override
	public boolean isInitialized(Context context) {
		return proxy != null;
	}

	private ContextModelProxy createProxy(Context context) {
		L.d("creating context proxy");
		Model model = TDBAwareOntDocumentManager.getInstance().getModel(PervADsContextModel.URI);
		return ContextModelFactory.createProxy(model);
	}
}
