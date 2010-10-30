package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public interface Dimension extends ContextModelEntity, ValueParent {
	public Resource getAssignmentClass();

	public Resource getValuesClass();

	public Property getAssignmentProperty();

	public Resource getFormalDimensionIndividual();

	public Value getParentValue();

	public int getDepth();

	public int getDistance(Dimension dimension);
}
