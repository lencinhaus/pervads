package it.polimi.dei.dbgroup.pedigree.pervads.client.android.query;

import java.util.ArrayList;
import java.util.List;

public class QueryResult {
	private String queryName;
	private List<MatchingPervAD> matchingPervADs = new ArrayList<MatchingPervAD>();

	private QueryResult() {
	}

	public QueryResult(String queryName) {
		this.queryName = queryName;
	}

	public String getQueryName() {
		return queryName;
	}

	public List<MatchingPervAD> getMatchingPervADs() {
		return matchingPervADs;
	}

}
