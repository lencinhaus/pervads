package it.polimi.dei.dbgroup.pedigree.contextmodel.matching.specialized;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;

import java.util.HashSet;
import java.util.Set;

public class ValueBasedCompatibilityChecker implements CompatibilityChecker {
	private final Set<String> compatibleValueURIs = new HashSet<String>();

	@Override
	public boolean isCompatible(Value sourceValue, Value targetValue) {
		return compatibleValueURIs.contains(sourceValue.getURI())
				&& compatibleValueURIs.contains(targetValue.getURI());
	}

	public void addCompatibleValue(Value value) {
		addCompatibleValue(value.getURI());
	}

	public void addCompatibleValue(String valueURI) {
		compatibleValueURIs.add(valueURI);
	}

}
