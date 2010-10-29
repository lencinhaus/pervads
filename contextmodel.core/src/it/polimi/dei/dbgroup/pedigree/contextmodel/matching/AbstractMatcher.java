package it.polimi.dei.dbgroup.pedigree.contextmodel.matching;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Assignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.AssignmentDefinition;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMatcher {
	private List<? extends AssignmentDefinition> definitions;

	public AbstractMatcher() {
		this.definitions = new ArrayList<AssignmentDefinition>();
	}

	public AbstractMatcher(List<? extends AssignmentDefinition> definitions) {
		this.definitions = definitions;
	}

	public List<? extends AssignmentDefinition> getDefinitions() {
		return definitions;
	}

	public void setDefinitions(List<? extends AssignmentDefinition> definitions) {
		this.definitions = definitions;
	}

	public Matching match() {
		double matchingScoreSum = 0;
		List<AssignmentDefinition> unmatchedDefinitions = new ArrayList<AssignmentDefinition>();
		List<AssignmentMatching> matchings = new ArrayList<AssignmentMatching>();

		for (AssignmentDefinition definition : definitions) {
			Assignment assignment = findMatchingAssignment(definition);

			if (assignment != null) {
				AssignmentMatching matching = createAssignmentMatching(
						definition, assignment);
				matchingScoreSum += matching.getScore();
				matchings.add(matching);
			} else
				unmatchedDefinitions.add(definition);
		}

		double score = matchingScoreSum / ((double) definitions.size());
		return new Matching(matchings, unmatchedDefinitions, score);
	}

	protected abstract Assignment findMatchingAssignment(
			AssignmentDefinition definition);

	private AssignmentMatching createAssignmentMatching(
			AssignmentDefinition definition, Assignment assignment) {
		int distance = assignment.getDefinition().getDimension().getDistance(
				definition.getDimension());
		double score = 1 / ((double) (1 + distance));
		score *= getCustomMatchingScore(definition, assignment);
		return new AssignmentMatching(definition, assignment, score);
	}

	protected double getCustomMatchingScore(AssignmentDefinition definition,
			Assignment assignment) {
		return 1;
	}
}
