package it.polimi.dei.dbgroup.pedigree.pervads.model.proxy;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextModelProxy;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.impl.PervADsModelProxyImpl;

import com.hp.hpl.jena.ontology.OntModel;

public class PervADsModelFactory {
	public static final String PERVADS_MODEL_ALT_URI = "owl/pervads_model.owl";
	private PervADsModelFactory() {
		
	}
	
	public static PervADsModelProxy createProxy(ContextModelProxy contextModel, OntModel model) {
		return new PervADsModelProxyImpl(contextModel, model);
	}
}
