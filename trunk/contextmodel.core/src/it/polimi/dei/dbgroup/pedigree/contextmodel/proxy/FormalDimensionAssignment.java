package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

public interface FormalDimensionAssignment extends
		DimensionAssignment<FormalParameterAssignment> {
	public void setParameterValue(ValueParameter parameter, Object value);

	public void addParameterAssignment(FormalParameterAssignment assignment);
}
