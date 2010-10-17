package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import com.hp.hpl.jena.ontology.Individual;

public interface Value extends ContextEntity, DimensionParent {
	public Individual getValueIndividual();

	public Dimension getParentDimension();

	public Dimension getRootDimension();
}
