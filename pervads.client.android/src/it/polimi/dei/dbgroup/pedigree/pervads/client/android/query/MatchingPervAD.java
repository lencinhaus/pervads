package it.polimi.dei.dbgroup.pedigree.pervads.client.android.query;

import java.util.List;

public class MatchingPervAD {
	private double score;
	private List<MatchingAssignment> assignmentMatchings;
	private PervADProxy pervAD;

	@SuppressWarnings("unused")
	private MatchingPervAD() {

	}

	protected MatchingPervAD(double score,
			List<MatchingAssignment> assignmentMatchings,
			PervADProxy pervAD) {
		super();
		this.score = score;
		this.assignmentMatchings = assignmentMatchings;
		this.pervAD = pervAD;
	}

	public double getScore() {
		return score;
	}

	public List<MatchingAssignment> getAssignmentMatchings() {
		return assignmentMatchings;
	}

	public PervADProxy getPervAD() {
		return pervAD;
	}

}
