package it.polimi.dei.dbgroup.pedigree.contextmodel.matching.specialized;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;

import java.util.ArrayList;
import java.util.List;

public class CompositeCompatibilityChecker implements CompatibilityChecker {
	public static enum Mode {
		ANY, ALL
	}

	private List<CompatibilityChecker> checkers = new ArrayList<CompatibilityChecker>();
	private Mode mode;

	public CompositeCompatibilityChecker(Mode mode) {
		this.mode = mode;
	}

	public CompositeCompatibilityChecker() {
		this(Mode.ALL);
	}

	@Override
	public boolean isCompatible(Value sourceValue, Value targetValue) {
		for (CompatibilityChecker checker : checkers) {
			boolean result = checker.isCompatible(sourceValue, targetValue);
			if (mode == Mode.ALL) {
				if (!result)
					return false;
			} else if (result)
				return true;
		}
		if (mode == Mode.ALL)
			return true;
		else
			return false;
	}

	public boolean addChecker(CompatibilityChecker checker) {
		return checkers.add(checker);
	}
}
