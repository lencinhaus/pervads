package it.polimi.dei.dbgroup.pedigree.pervads.client.android.query;

public class LightweightAssignmentMatching {
	private double score;
	private LightweightAssignment assignment;
	
	private LightweightAssignmentMatching() {
		
	}

	protected LightweightAssignmentMatching(double score,
			LightweightAssignment assignment) {
		super();
		this.score = score;
		this.assignment = assignment;
	}

	public double getScore() {
		return score;
	}

	public LightweightAssignment getAssignment() {
		return assignment;
	}

}
