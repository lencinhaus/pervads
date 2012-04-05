package it.polimi.dei.dbgroup.pedigree.contextmodel.matching;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.DimensionAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ParameterAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.util.StringUtils;

public class DimensionAssignmentMatching<S extends DimensionAssignment<? extends ParameterAssignment>, T extends DimensionAssignment<? extends ParameterAssignment>> {
	private S source;
	private T target;
	private double score;

	protected DimensionAssignmentMatching(S source, T target, double score) {
		super();
		this.source = source;
		this.target = target;
		this.score = score;
	}

	public S getSource() {
		return source;
	}

	public T getTarget() {
		return target;
	}

	public double getScore() {
		return score;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb, 0);
		return sb.toString();
	}
	
	void toString(StringBuilder sb, int indent) {
		StringUtils.indent(sb, indent, "DimensionAssignmentMatching");
		StringUtils.indent(sb, ++indent, "Score: ");
		sb.append(score);
		sb.append("\n");
		StringUtils.indent(sb, ++indent, "Source: ");
		sb.append(source);
		sb.append("\n");
		StringUtils.indent(sb, ++indent, "Target: ");
		sb.append(target);
		sb.append("\n");
	}
}
