package it.polimi.dei.dbgroup.pedigree.contextmodel.matching.specialized;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;

import java.util.ArrayList;
import java.util.List;

public class ClassBasedCompatibilityChecker implements CompatibilityChecker {
	private final List<String> compatibleClassURIs = new ArrayList<String>();

	@Override
	public boolean isCompatible(Value sourceValue, Value targetValue) {
		return checkClasses(sourceValue) && checkClasses(targetValue);
	}

	private boolean checkClasses(Value value) {
		for (String uri : compatibleClassURIs) {
			if (value.hasClass(uri))
				return true;
		}
		return false;
	}

	public void addCompatibleClass(String classURI) {
		compatibleClassURIs.add(classURI);
	}

}
