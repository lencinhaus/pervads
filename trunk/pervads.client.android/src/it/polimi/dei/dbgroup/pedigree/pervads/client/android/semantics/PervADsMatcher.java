package it.polimi.dei.dbgroup.pedigree.pervads.client.android.semantics;

import it.polimi.dei.dbgroup.pedigree.contextmodel.matching.SourceMatcher;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Assignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.AssignmentDefinition;

public class PervADsMatcher extends SourceMatcher {
	public PervADsMatcher() {
		super();
	}

	@Override
	protected double getCustomMatchingScore(AssignmentDefinition definition,
			Assignment assignment) {
		// TODO implement special dimensions matching
		return super.getCustomMatchingScore(definition, assignment);
	}
}
