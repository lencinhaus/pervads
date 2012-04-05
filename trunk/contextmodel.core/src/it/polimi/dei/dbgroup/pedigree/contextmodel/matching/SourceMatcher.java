package it.polimi.dei.dbgroup.pedigree.contextmodel.matching;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Dimension;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.DimensionAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.DimensionAssignmentSource;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ParameterAssignment;

import java.util.Collection;

public class SourceMatcher<S extends DimensionAssignment<? extends ParameterAssignment>, T extends DimensionAssignment<? extends ParameterAssignment>>
		extends AbstractMatcher<S, T> {
	private DimensionAssignmentSource<T> source;

	public SourceMatcher() {
		this(null, null);
	}

	public SourceMatcher(Collection<S> sourceDimensionAssignments) {
		this(sourceDimensionAssignments, null);
	}

	public SourceMatcher(Collection<S> sourceDimensionAssignments,
			DimensionAssignmentSource<T> source) {
		super(sourceDimensionAssignments);
		this.source = source;
	}

	public DimensionAssignmentSource<T> getSource() {
		return source;
	}

	public void setSource(DimensionAssignmentSource<T> source) {
		this.source = source;
	}

	@Override
	protected T findTargetAssignmentForDimension(Dimension dimension) {
		return source.findAssignmentForDimension(dimension);
	}
}
