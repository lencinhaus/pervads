package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

public interface DimensionAssignmentSource<D extends DimensionAssignment<? extends ParameterAssignment>> {
	public D findAssignmentForDimension(Dimension dimension);
}
