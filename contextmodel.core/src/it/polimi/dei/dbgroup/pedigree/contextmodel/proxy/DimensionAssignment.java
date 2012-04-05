package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import java.util.Collection;

public interface DimensionAssignment<P extends ParameterAssignment> {
	public Dimension getDimension();
	
	public Value getValue();

	public Object getParameterValue(ValueParameter parameter);

	public Object getParameterValue(String formalParameterIndividualUri);

	public Collection<? extends P> listParameterAssignments();
}
