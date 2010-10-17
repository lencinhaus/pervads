package it.polimi.dei.dbgroup.pedigree.pervads.query;

import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class QueryManager {
	private static final String QUERIES_FILE = "queries.dat";
	private static List<Query> queries = null;
	private Context context;

	public QueryManager(Context context) {
		this.context = context;
	}

	public List<Query> getQueries() {
		synchronized (this) {
			if (queries == null) {
				deserializeQueries();
			}
		}
		return queries;
	}

	public void writeQueries() {
		synchronized (this) {
			serializeQueries();
		}
	}

	public void synchronize() {
		synchronized (this) {
			queries = null;
		}
	}

	private void deserializeQueries() {
		ObjectInputStream is = null;
		try {
			is = new ObjectInputStream(context.openFileInput(QUERIES_FILE));
			queries = (List<Query>) is.readObject();
		} catch (FileNotFoundException fnex) {
			// it's ok, the queries have never been serialized
			queries = new ArrayList<Query>();
		} catch (Exception ex) {
			throw new RuntimeException("cannot deserialize queries from "
					+ QUERIES_FILE, ex);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception ex) {
					throw new RuntimeException(
							"cannot close queries input stream", ex);
				}
			}
		}
	}

	private void serializeQueries() {
		ObjectOutputStream os = null;
		try {
			os = new ObjectOutputStream(context.openFileOutput(QUERIES_FILE,
					Context.MODE_PRIVATE));
			os.writeObject(queries);
		} catch (Exception ex) {
			throw new RuntimeException("cannot serialize queries to "
					+ QUERIES_FILE, ex);
		} finally {
			try {
				os.close();
			} catch (Exception ex) {
				throw new RuntimeException(
						"cannot close queries output stream", ex);
			}
		}
	}
}
