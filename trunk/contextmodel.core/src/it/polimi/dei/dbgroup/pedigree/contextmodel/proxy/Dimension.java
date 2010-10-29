package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;

public interface Dimension extends ContextModelEntity, ValueParent {
	public OntClass getAssignmentClass();

	public OntClass getValuesClass();

	public OntProperty getAssignmentProperty();

	public Individual getFormalDimensionIndividual();

	public Value getParentValue();
	
	public int getDepth();
	
	public int getDistance(Dimension dimension);
}
