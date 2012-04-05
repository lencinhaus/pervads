package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import com.hp.hpl.jena.rdf.model.Resource;

public interface Dimension extends ContextModelEntity, ValueParent {
	public Resource getActualDimensionClass();

	public Resource getValuesClass();

	public Resource getFormalDimensionIndividual();

	public Value getParentValue();

	public int getDepth();

	public int getDistance(Dimension dimension);
}
