package it.polimi.dei.dbgroup.pedigree.pervads.client.android.query;

import java.util.List;

public class MatchingPervAD {
	private double score;
	private List<LightweightAssignmentMatching> assignmentMatchings;
	private LightweightPervAD pervAD;

	private MatchingPervAD() {

	}

	protected MatchingPervAD(double score,
			List<LightweightAssignmentMatching> assignmentMatchings,
			LightweightPervAD pervAD) {
		super();
		this.score = score;
		this.assignmentMatchings = assignmentMatchings;
		this.pervAD = pervAD;
	}

	public double getScore() {
		return score;
	}

	public List<LightweightAssignmentMatching> getAssignmentMatchings() {
		return assignmentMatchings;
	}

	public LightweightPervAD getPervAD() {
		return pervAD;
	}

}
