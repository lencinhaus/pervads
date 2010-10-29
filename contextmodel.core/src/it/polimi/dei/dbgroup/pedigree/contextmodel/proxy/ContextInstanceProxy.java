package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;


public interface ContextInstanceProxy {
	public OntModel getModel();
	public List<? extends Context> listContexts();
	public Context getContext(String uri);
	public ContextModelProxy getContextModel();
}
