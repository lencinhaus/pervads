package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import com.hp.hpl.jena.rdf.model.Resource;

public interface Value extends ContextModelEntity, DimensionParent {
	public Resource getValueIndividual();

	public Dimension getParentDimension();

	public Dimension getRootDimension();
}
