package it.polimi.dei.dbgroup.pedigree.contextmodel.matching.specialized;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.DimensionAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ParameterAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SpecializedMatcherResolver<S extends DimensionAssignment<? extends ParameterAssignment>, T extends DimensionAssignment<? extends ParameterAssignment>> {
	private static class DoubleStringKey {
		public String first;
		public String second;

		public DoubleStringKey(String first, String second) {
			this.first = first;
			this.second = second;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof DoubleStringKey) {
				DoubleStringKey key = (DoubleStringKey) obj;

				boolean firstEquals = isEmpty(first) ? isEmpty(key.first)
						: first.equals(key.first);
				boolean secondEquals = isEmpty(second) ? isEmpty(key.second)
						: second.equals(key.second);
				return firstEquals && secondEquals;
			}
			return super.equals(obj);
		}

		private static boolean isEmpty(String s) {
			return s == null || s.isEmpty();
		}

		@Override
		public int hashCode() {
			int firstHash = isEmpty(first) ? 0 : first.hashCode();
			int secondHash = isEmpty(second) ? 0 : second.hashCode();
			return firstHash ^ secondHash;
		}
	}

	private Collection<SpecializedMatcher<S, T>> matchers = new ArrayList<SpecializedMatcher<S, T>>();
	private Map<DoubleStringKey, SpecializedMatcher<S, T>> map = new HashMap<DoubleStringKey, SpecializedMatcher<S, T>>();

	public SpecializedMatcher<S, T> findSpecializedMatcher(Value sourceValue,
			Value targetValue) {
		DoubleStringKey key = new DoubleStringKey(sourceValue.getURI(),
				targetValue.getURI());
		SpecializedMatcher<S, T> foundMatcher = map.get(key);
		if (foundMatcher == null) {
			for (SpecializedMatcher<S, T> matcher : matchers) {
				if (matcher.getCompatibilityChecker().isCompatible(sourceValue, targetValue)) {
					map.put(key, matcher);
					foundMatcher = matcher;
					break;
				}
			}
		}
		return foundMatcher;
	}

	public void addMatcher(SpecializedMatcher<S, T> matcher) {
		matchers.add(matcher);
	}
}
