package it.polimi.dei.dbgroup.pedigree.contextmodel.matching.specialized;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;

public interface CompatibilityChecker {
	public boolean isCompatible(Value sourceValue, Value targetValue);
}
