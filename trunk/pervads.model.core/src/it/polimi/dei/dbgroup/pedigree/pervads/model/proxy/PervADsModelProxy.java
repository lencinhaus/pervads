package it.polimi.dei.dbgroup.pedigree.pervads.model.proxy;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextModelProxy;

import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;

public interface PervADsModelProxy {
	public ContextModelProxy getContextModel();
	public OntModel getModel();
	public List<? extends PervAD> listPervADs();
}
