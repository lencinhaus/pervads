package it.polimi.dei.dbgroup.pedigree.contextmodel.matching;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.AssignmentDefinition;

import java.util.List;

public class Matching {
	private List<AssignmentMatching> assignmentMatchings;
	private List<AssignmentDefinition> unmatchedDefinitions;
	private double score;

	protected Matching(List<AssignmentMatching> assignmentMatchings,
			List<AssignmentDefinition> unmatchedDefinitions, double score) {
		super();
		this.assignmentMatchings = assignmentMatchings;
		this.unmatchedDefinitions = unmatchedDefinitions;
		this.score = score;
	}

	public List<AssignmentMatching> getAssignmentMatchings() {
		return assignmentMatchings;
	}

	public double getScore() {
		return score;
	}

	public List<AssignmentDefinition> getUnmatchedDefinitions() {
		return unmatchedDefinitions;
	}
}
