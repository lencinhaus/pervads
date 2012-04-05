package it.polimi.dei.dbgroup.pedigree.pervads.client.android.query;

import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.SerializedDataManager;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.thoughtworks.xstream.XStream;

public class ResultManager extends SerializedDataManager<List<QueryResult>> {
	public ResultManager(Context context) {
		super(context);
	}

	public List<QueryResult> getResults() {
		return getData();
	}

	public void setResults(List<QueryResult> results) {
		setData(results);
		writeData();
	}

	@Override
	protected List<QueryResult> getInitialData() {
		return new ArrayList<QueryResult>();
	}

	@Override
	protected String getName() {
		return "results";
	}

	@Override
	protected void initializeXStream(XStream xstream) {
		xstream.alias("result", QueryResult.class);
		xstream.alias("matching", MatchingPervAD.class);
		xstream.alias("assignmentMatching", MatchingAssignment.class);
		xstream.alias("assignment", AssignmentProxy.class);
		xstream.alias("pervad", PervADProxy.class);
	}

}
