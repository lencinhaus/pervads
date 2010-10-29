package it.polimi.dei.dbgroup.pedigree.contextmodel.matching;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Assignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.AssignmentDefinition;

public class AssignmentMatching {
	private AssignmentDefinition definition;
	private Assignment matched;
	private double score;

	protected AssignmentMatching(AssignmentDefinition definition,
			Assignment matched, double score) {
		super();
		this.definition = definition;
		this.matched = matched;
		this.score = score;
	}

	public AssignmentDefinition getDefinition() {
		return definition;
	}

	public Assignment getMatched() {
		return matched;
	}

	public double getScore() {
		return score;
	}
}
