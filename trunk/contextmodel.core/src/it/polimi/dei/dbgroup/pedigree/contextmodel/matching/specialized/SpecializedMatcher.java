package it.polimi.dei.dbgroup.pedigree.contextmodel.matching.specialized;

import it.polimi.dei.dbgroup.pedigree.contextmodel.matching.DimensionAssignmentMatcher;
import it.polimi.dei.dbgroup.pedigree.contextmodel.matching.IncompatibleAssignmentsException;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.DimensionAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ParameterAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;

public abstract class SpecializedMatcher<S extends DimensionAssignment<? extends ParameterAssignment>, T extends DimensionAssignment<? extends ParameterAssignment>>
		implements DimensionAssignmentMatcher<S, T> {
	private CompatibilityChecker compatibilityChecker = FailingChecker;
	private static final CompatibilityChecker FailingChecker = new CompatibilityChecker() {

		@Override
		public boolean isCompatible(Value sourceValue, Value targetValue) {
			return false;
		}

	};

	public void setCompatibilityChecker(
			CompatibilityChecker compatibilityChecker) {
		if (compatibilityChecker == null)
			throw new NullPointerException("compabilityChecker cannot be null");
		this.compatibilityChecker = compatibilityChecker;
	}

	public CompatibilityChecker getCompatibilityChecker() {
		return compatibilityChecker;
	}

	@Override
	public abstract double match(S sourceAssignment, T targetAssignment)
			throws IncompatibleAssignmentsException;

}
