package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import com.hp.hpl.jena.rdf.model.Resource;

public interface Assignment extends ContextInstanceEntity {
	public AssignmentDefinition getDefinition();

	public Resource getAssignmentIndividual();

	public boolean isCompatible(AssignmentDefinition definition);
}
