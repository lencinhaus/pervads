package it.polimi.dei.dbgroup.pedigree.pervads.model.matching.specialized;

import it.polimi.dei.dbgroup.pedigree.contextmodel.matching.specialized.TypedMatcher;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.DimensionAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ParameterAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ValueParameter;
import it.polimi.dei.dbgroup.pedigree.pervads.model.matching.specialized.ScalarValue.Type;

public abstract class ScalarMatcher<S extends DimensionAssignment<? extends ParameterAssignment>, T extends DimensionAssignment<? extends ParameterAssignment>, V extends ScalarValue>
		extends TypedMatcher<S, T, V> {

	public ScalarMatcher(Class<V> valueClass) {
		super(valueClass);
	}

	@Override
	protected double matchValues(V source, V target) {
		if (source.getType() != target.getType()) {
			// exact vs interval
			if (source.getType() == Type.EXACT)
				return (source.getScalar() >= target.getStart() && source
						.getScalar() <= target.getEnd()) ? 1 : 0;
			else
				return (target.getScalar() >= source.getStart() && target
						.getScalar() <= source.getEnd()) ? 1 : 0;
		} else if (source.getType() == Type.EXACT) {
			// both exact
			return (source.getScalar() == target.getScalar()) ? 1 : 0;
		} else {
			// both intervals
			return computeOverlapping(source.getStart(), source.getEnd(),
					target.getStart(), target.getEnd());
		}
	}

	protected double computeOverlapping(double start1, double end1,
			double start2, double end2) {
		if (start2 > end1 || end2 < start1)
			return 0;
		double len = end1 - start1;
		double start = Math.max(start1, start2);
		double end = Math.min(end1, end2);
		return (end - start) / len;
	}

	@Override
	protected Object convertParameterValue(
			ValueParameter parameter, Object value) {
		return convertParameterToScalar(parameter, value);
	}

	protected double convertParameterToScalar(ValueParameter parameter,
			Object value) {
		if (value instanceof Double)
			return (Double) value;
		throw new RuntimeException("cannot convert value " + value
				+ " of parameter " + parameter + " to double");
	}
}
