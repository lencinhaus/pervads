package it.polimi.dei.dbgroup.pedigree.contextmodel.matching;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.DimensionAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ParameterAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.util.StringUtils;

import java.util.Collection;

public class Matching<S extends DimensionAssignment<? extends ParameterAssignment>, T extends DimensionAssignment<? extends ParameterAssignment>> {
	private Collection<DimensionAssignmentMatching<S, T>> matchings;
	private Collection<S> unmatchedAssignments;
	private double score;

	protected Matching(Collection<DimensionAssignmentMatching<S, T>> matchings,
			Collection<S> unmatchedAssignments, double score) {
		super();
		this.matchings = matchings;
		this.unmatchedAssignments = unmatchedAssignments;
		this.score = score;
	}

	public Collection<DimensionAssignmentMatching<S, T>> getMatchings() {
		return matchings;
	}

	public double getScore() {
		return score;
	}

	public Collection<S> getUnmatchedAssignments() {
		return unmatchedAssignments;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb, 0);
		return sb.toString();
	}

	void toString(StringBuilder sb, int indent) {
		StringUtils.indent(sb, indent, "Matching");
		StringUtils.indent(sb, ++indent, "Score: ", false);
		sb.append(score);
		sb.append("\n");
		if (matchings.size() > 0) {
			StringUtils.indent(sb, indent, "Dimension assignment matchings:");
			for (DimensionAssignmentMatching<S, T> matching : matchings) {
				matching.toString(sb, indent + 1);
			}
		}
		if (unmatchedAssignments.size() > 0) {
			StringUtils.indent(sb, indent, "Unmatched dimension assignments:");
			for (S unmatched : unmatchedAssignments) {
				StringUtils.indent(sb, indent + 1, unmatched.toString());
			}
		}
	}
}
