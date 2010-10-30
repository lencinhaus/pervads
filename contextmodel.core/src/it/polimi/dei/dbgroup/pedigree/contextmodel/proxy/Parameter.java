package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import com.hp.hpl.jena.rdf.model.Resource;

public interface Parameter extends ContextModelEntity {
	public Resource getAssignmentClass();

	public Resource getFormalParameterIndividual();
}
