package it.polimi.dei.dbgroup.pedigree.pervads.client.android.query;

import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.SerializedDataManager;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.thoughtworks.xstream.XStream;

public class QueryManager extends SerializedDataManager<List<Query>> {

	public QueryManager(Context context) {
		super(context);
	}

	public List<Query> getQueries() {
		return getData();
	}

	public void writeQueries() {
		writeData();
	}

	@Override
	protected List<Query> getInitialData() {
		return new ArrayList<Query>();
	}

	@Override
	protected String getName() {
		return "queries";
	}

	@Override
	protected void initializeXStream(XStream xstream) {
		xstream.alias("query", Query.class);
		xstream.alias("assignment", AssignmentProxy.class);
	}
}
