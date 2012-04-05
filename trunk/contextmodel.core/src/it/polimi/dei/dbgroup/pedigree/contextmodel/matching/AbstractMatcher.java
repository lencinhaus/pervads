package it.polimi.dei.dbgroup.pedigree.contextmodel.matching;

import it.polimi.dei.dbgroup.pedigree.contextmodel.matching.specialized.SpecializedMatcherResolver;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Dimension;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.DimensionAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ParameterAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractMatcher<S extends DimensionAssignment<? extends ParameterAssignment>, T extends DimensionAssignment<? extends ParameterAssignment>> {
	private Collection<S> sourceDimensionAssignments;
	private SpecializedMatcherResolver<S, T> specializedMatcherResolver = new SpecializedMatcherResolver<S, T>();

	public SpecializedMatcherResolver<S, T> getSpecializedMatcherResolver() {
		return specializedMatcherResolver;
	}

	private final DimensionAssignmentMatcher<S, T> StandardMatcher = new DimensionAssignmentMatcher<S, T>() {
		@Override
		public double match(S sourceAssignment, T targetAssignment)
				throws IncompatibleAssignmentsException {
			if (!sourceAssignment.equals(targetAssignment))
				throw new IncompatibleAssignmentsException();
			return 1D / (1D + (double) getDistance(sourceAssignment
					.getDimension()));
		}

		private int getDistance(Dimension dimension) {
			int distance = 0;
			for (Value value : dimension.listChildValues()) {
				for (Dimension subDimension : value.listChildDimensions()) {
					if (findTargetAssignmentForDimension(subDimension) != null)
						distance += 1 + getDistance(subDimension);
				}
			}

			return distance;
		}
	};

	public AbstractMatcher() {
		this(null);
	}

	public AbstractMatcher(Collection<S> sourceDimensionAssignments) {
		if(sourceDimensionAssignments == null) sourceDimensionAssignments = new ArrayList<S>();
		this.sourceDimensionAssignments = sourceDimensionAssignments;
	}

	public Collection<S> getSourceDimensionAssignments() {
		return sourceDimensionAssignments;
	}

	public void setSourceDimensionAssignments(
			Collection<S> sourceDimensionAssignments) {
		this.sourceDimensionAssignments = sourceDimensionAssignments;
	}

	public Matching<S, T> match() {
		double matchingScoreSum = 0;
		List<S> unmatchedAssignments = new ArrayList<S>();
		List<DimensionAssignmentMatching<S, T>> matchings = new ArrayList<DimensionAssignmentMatching<S, T>>();

		for (S source : sourceDimensionAssignments) {
			double assignmentMatchingScore = 0;
			Dimension dimension = source.getDimension();
			T target = findTargetAssignmentForDimension(dimension);
			if (target != null) {
				DimensionAssignmentMatcher<S, T> matcher = specializedMatcherResolver
						.findSpecializedMatcher(source.getValue(), target
								.getValue());
				if (matcher == null)
					matcher = StandardMatcher;
				try {
					assignmentMatchingScore = matcher.match(source, target);
					DimensionAssignmentMatching<S, T> matching = new DimensionAssignmentMatching<S, T>(
							source, target, assignmentMatchingScore);
					matchings.add(matching);
				} catch (IncompatibleAssignmentsException ex) {
					// invalidate the whole matching
					matchingScoreSum = 0;
					matchings.clear();
					unmatchedAssignments.clear();
					unmatchedAssignments.addAll(sourceDimensionAssignments);
					break;
				}
			} else
				unmatchedAssignments.add(source);

			matchingScoreSum += assignmentMatchingScore;
		}

		double totalMatchingScore = matchingScoreSum
				/ ((double) sourceDimensionAssignments.size());

		return new Matching<S, T>(matchings, unmatchedAssignments,
				totalMatchingScore);
	}

	protected abstract T findTargetAssignmentForDimension(Dimension dimension);
}
