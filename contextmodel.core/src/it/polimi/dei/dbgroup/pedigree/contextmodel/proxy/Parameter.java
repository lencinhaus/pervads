package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;

public interface Parameter extends ContextModelEntity {
	public OntClass getAssignmentClass();

	public Individual getFormalParameterIndividual();
}
