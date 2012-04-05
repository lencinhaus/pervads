package it.polimi.dei.dbgroup.pedigree.contextmodel.matching;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.DimensionAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ParameterAssignment;

public interface DimensionAssignmentMatcher<S extends DimensionAssignment<? extends ParameterAssignment>, T extends DimensionAssignment<? extends ParameterAssignment>> {
	public double match(S sourceAssignment, T targetAssignment) throws IncompatibleAssignmentsException;
}
