package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;

public interface Parameter extends ContextEntity {
	public OntClass getAssignmentClass();

	public Individual getFormalParameterIndividual();
}
