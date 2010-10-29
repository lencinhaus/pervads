package it.polimi.dei.dbgroup.pedigree.contextmodel.matching;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Assignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.AssignmentDefinition;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.AssignmentSource;

import java.util.List;

public class SourceMatcher extends AbstractMatcher {
	private AssignmentSource source;
	
	public SourceMatcher() {
		super();
		this.source = null;
	}

	public SourceMatcher(List<? extends AssignmentDefinition> definitions) {
		super(definitions);
		this.source = null;
	}

	public SourceMatcher(List<? extends AssignmentDefinition> definitions,
			AssignmentSource source) {
		super(definitions);
		this.source = source;
	}

	public AssignmentSource getSource() {
		return source;
	}

	public void setSource(AssignmentSource source) {
		this.source = source;
	}

	@Override
	protected Assignment findMatchingAssignment(AssignmentDefinition definition) {
		if(source == null) throw new IllegalStateException("source not set");
		List<? extends Assignment> matchingAssignments = source
				.findCompatibleAssignments(definition);
		if (matchingAssignments.size() > 1)
			throw new RuntimeException(
					"source returned more than 1 compatible assignment for definition "
							+ definition);
		if (matchingAssignments.size() > 0)
			return matchingAssignments.get(0);
		return null;
	}

}
