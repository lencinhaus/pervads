package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import java.util.Collection;

import com.hp.hpl.jena.rdf.model.Resource;

public interface Value extends ContextModelEntity, DimensionParent {
	public Resource getValueIndividual();

	public Dimension getParentDimension();

	public Dimension getRootDimension();

	public Collection<? extends ValueParameter> listParameters();
	
	public ValueParameter findParameter(String formalParameterIndividualUri);
}
