package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import com.hp.hpl.jena.ontology.Individual;

public interface Assignment extends ContextInstanceEntity {
	public AssignmentDefinition getDefinition();

	public Individual getAssignmentIndividual();
	
	public boolean isCompatible(AssignmentDefinition definition);
}
