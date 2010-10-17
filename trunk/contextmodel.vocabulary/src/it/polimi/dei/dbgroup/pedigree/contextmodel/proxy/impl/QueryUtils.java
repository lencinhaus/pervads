package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;

public class QueryUtils {
	public static final String SPARQL_FOLDER_NAME = "sparql";
	public static final String QUERY_FILE_EXT = "rq";
	private static final Map<String, String> queryMap = new HashMap<String, String>();

	private QueryUtils() {

	}

	public static QueryExecution createQuery(Model model, String queryName,
			Object... args) throws IOException {
		String queryString = queryMap.get(queryName);
		if (queryString == null) {
			BufferedReader reader = null;
			String queryPath = SPARQL_FOLDER_NAME + "/" + queryName + "."
					+ QUERY_FILE_EXT;
			File queryFile = new File(queryPath);
			if (queryFile.exists()) {
				reader = new BufferedReader(new FileReader(queryFile));
			} else {
				InputStream is = QueryUtils.class.getClassLoader()
						.getResourceAsStream(queryPath);
				if (is == null)
					throw new IOException("Query file " + queryPath
							+ " not found");
				reader = new BufferedReader(new InputStreamReader(is));
			}
			StringBuilder sb = new StringBuilder();
			char[] buf = new char[1024];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				sb.append(buf, 0, numRead);
			}
			reader.close();
			queryString = sb.toString();
			queryMap.put(queryName, queryString);
		}
		queryString = String.format(queryString, args);
		Query query = QueryFactory.create(queryString);
		return QueryExecutionFactory.create(query, model);
	}
}
