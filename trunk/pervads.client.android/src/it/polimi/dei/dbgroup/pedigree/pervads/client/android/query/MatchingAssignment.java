package it.polimi.dei.dbgroup.pedigree.pervads.client.android.query;

public class MatchingAssignment {
	private double score;
	private AssignmentProxy assignment;
	
	@SuppressWarnings("unused")
	private MatchingAssignment() {
		
	}

	protected MatchingAssignment(double score,
			AssignmentProxy assignment) {
		super();
		this.score = score;
		this.assignment = assignment;
	}

	public double getScore() {
		return score;
	}

	public AssignmentProxy getAssignment() {
		return assignment;
	}

}
