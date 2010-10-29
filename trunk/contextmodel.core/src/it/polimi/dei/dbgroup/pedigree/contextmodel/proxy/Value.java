package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import com.hp.hpl.jena.ontology.Individual;

public interface Value extends ContextModelEntity, DimensionParent {
	public Individual getValueIndividual();

	public Dimension getParentDimension();

	public Dimension getRootDimension();
}
