package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;


public interface ContextInstanceProxy {
	public Model getModel();
	public List<? extends Context> listContexts();
	public Context getContext(String uri);
	public ContextModelProxy getContextModel();
}
