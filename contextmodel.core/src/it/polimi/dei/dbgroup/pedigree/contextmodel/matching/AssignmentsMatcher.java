package it.polimi.dei.dbgroup.pedigree.contextmodel.matching;

import java.util.List;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Assignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.AssignmentDefinition;

public class AssignmentsMatcher extends AbstractMatcher {
	private List<? extends Assignment> assignments;
	
	public AssignmentsMatcher() {
		super();
		this.assignments = null;
	}
	
	public AssignmentsMatcher(List<? extends AssignmentDefinition> definitions) {
		super(definitions);
		this.assignments = null;
	}
	
	public AssignmentsMatcher(List<? extends AssignmentDefinition> definitions, List<? extends Assignment> assignments) {
		super(definitions);
		this.assignments = assignments;
	}
	
	@Override
	protected Assignment findMatchingAssignment(AssignmentDefinition definition) {
		if(assignments == null) throw new IllegalStateException("assignments not set");
		Assignment compatible = null;
		for(Assignment assignment : assignments) {
			if(assignment.isCompatible(definition)) {
				if(compatible != null) throw new RuntimeException("more than one assignment is compatible with definition " + definition);
				compatible = assignment;
			}
		}
		
		return compatible;
	}

}
