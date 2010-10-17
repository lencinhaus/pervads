package it.polimi.dei.dbgroup.pedigree.pervads.context;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextModelProxy;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Dimension;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;
import it.polimi.dei.dbgroup.pedigree.pervads.Config;
import it.polimi.dei.dbgroup.pedigree.pervads.R;
import it.polimi.dei.dbgroup.pedigree.pervads.util.Initializable;
import it.polimi.dei.dbgroup.pedigree.pervads.util.ProgressMonitor;
import it.polimi.dei.dbgroup.pedigree.pervads.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import android.content.Context;
import android.text.TextUtils;

public class ContextSearchManager implements Initializable {
	private static ContextSearchManager instance = null;
	private static final String INDEX_DIRECTORY_NAME = "index";
	private static final String FIELD_VALUE_NAME = "value_name";
	private static final String FIELD_VALUE_DESCRIPTION = "value_description";
	private static final String FIELD_DIMENSION_NAME = "dimension_name";
	private static final String FIELD_DIMENSION_DESCRIPTION = "dimension_description";
	private static final String FIELD_URI = "uri";
	private static final float FUZZY_MINIMUM = .8F;
	// private final Logger L = new Logger(ContextSearchManager.class
	// .getSimpleName());
	private Value selectedValue = null;
	private OnValueSelectedListener onValueSelectedListener = null;
	private boolean initialized = false;

	public interface OnValueSelectedListener {
		public void onValueSelected(Value value);
	}

	private ContextSearchManager() {
	}

	public static ContextSearchManager getInstance() {
		if (instance == null)
			instance = new ContextSearchManager();
		return instance;
	}

	public List<? extends Value> search(String queryString, int maxResults,
			Context context, String... restrictedValueURIs) {
		List<Value> results = new ArrayList<Value>();
		ContextModelProxy proxy = ContextProxyManager.getInstance().getProxy();
		try {
			Directory directory = FSDirectory.open(getIndexDirectory(context));
			IndexSearcher searcher = new IndexSearcher(directory, true);
			Query query = buildQuery(queryString, restrictedValueURIs);
			ScoreDoc[] hits = searcher.search(query, maxResults).scoreDocs;
			for (ScoreDoc scoreDoc : hits) {
				Document doc = searcher.doc(scoreDoc.doc);
				String uri = doc.get(FIELD_URI);
				Value value = proxy.findValue(uri);
				results.add(value);
			}
			searcher.close();
			directory.close();
		} catch (IOException ex) {
			throw new RuntimeException(
					"an error occurred while reading context index", ex);
		}

		return results;
	}

	private Query buildQuery(String queryString, String[] restrictedValueURIs) {
		String[] termStrings = queryString.split(" ");
		BooleanQuery query = new BooleanQuery();
		for (String termString : termStrings) {
			termString = termString.trim();
			if (termString.length() == 0)
				continue;
			BooleanQuery termQuery = new BooleanQuery();
			termQuery.setMinimumNumberShouldMatch(1);
			Query valueNameQuery = new FuzzyQuery(new Term(FIELD_VALUE_NAME,
					termString), FUZZY_MINIMUM);
			// valueNameQuery.setBoost(1F);
			termQuery.add(valueNameQuery, Occur.SHOULD);
			Query valueDescriptionQuery = new FuzzyQuery(new Term(
					FIELD_VALUE_DESCRIPTION, termString), FUZZY_MINIMUM);
			// valueDescriptionQuery.setBoost(.5F);
			termQuery.add(valueDescriptionQuery, Occur.SHOULD);
			Query dimensionNameQuery = new FuzzyQuery(new Term(
					FIELD_DIMENSION_NAME, termString), FUZZY_MINIMUM);
			// dimensionNameQuery.setBoost(.7F);
			termQuery.add(dimensionNameQuery, Occur.SHOULD);
			Query dimensionDescriptionQuery = new FuzzyQuery(new Term(
					FIELD_DIMENSION_DESCRIPTION, termString), FUZZY_MINIMUM);
			// dimensionDescriptionQuery.setBoost(.35F);
			termQuery.add(dimensionDescriptionQuery, Occur.SHOULD);
			query.add(termQuery, Occur.MUST);
		}
		for (String restrictedValueURI : restrictedValueURIs) {
			TermQuery termQuery = new TermQuery(new Term(FIELD_URI,
					restrictedValueURI));
			query.add(termQuery, Occur.MUST_NOT);
		}
		return query;
	}

	public OnValueSelectedListener getOnValueSelectedListener() {
		return onValueSelectedListener;
	}

	public void setOnValueSelectedListener(
			OnValueSelectedListener onValueSelectedListener) {
		this.onValueSelectedListener = onValueSelectedListener;
	}

	public Value getSelectedValue() {
		return selectedValue;
	}

	public void setSelectedValue(Value selectedValue) {
		this.selectedValue = selectedValue;

		// notify listener
		if (onValueSelectedListener != null)
			onValueSelectedListener.onValueSelected(selectedValue);
	}

	@Override
	public boolean isInitialized(Context context) {
		File indexDirectory = getIndexDirectory(context);
		if (Config.DEBUG_RESET) {
			if (!initialized) {
				Utils.recursiveDelete(indexDirectory);
				initialized = true;
			}
		}
		// check if the index file exists
		return indexDirectory.exists();
	}

	@Override
	public void initialize(Context context, ProgressMonitor monitor) {
		// collect all dimensions and values
		ContextModelProxy proxy = ContextProxyManager.getInstance().getProxy();
		monitor.indeterminate(true);
		monitor.message(R.string.context_search_manager_operation_build_index);

		// collect dimensions
		Collection<? extends Dimension> dimensions = proxy.findAllDimensions();

		// build the index
		monitor.indeterminate(false);
		monitor.max(dimensions.size());
		monitor.progress(0);
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
		try {
			Directory directory = FSDirectory.open(getIndexDirectory(context));
			IndexWriter writer = new IndexWriter(directory, analyzer, true,
					IndexWriter.MaxFieldLength.LIMITED);
			for (Dimension dimension : dimensions) {
				Field dimensionNameField = new Field(FIELD_DIMENSION_NAME,
						dimension.getName(), Field.Store.YES,
						Field.Index.ANALYZED);
				dimensionNameField.setBoost(.7F);
				Field dimensionDescriptionField = null;
				if (!TextUtils.isEmpty(dimension.getDescription())) {
					dimensionDescriptionField = new Field(
							FIELD_DIMENSION_DESCRIPTION, dimension
									.getDescription(), Field.Store.YES,
							Field.Index.ANALYZED);
					dimensionDescriptionField.setBoost(.35F);
				}
				for (Value value : dimension.listChildValues()) {
					Document doc = new Document();
					doc.add(dimensionNameField);
					if (dimensionDescriptionField != null)
						doc.add(dimensionDescriptionField);
					Field valueNameField = new Field(FIELD_VALUE_NAME, value
							.getName(), Field.Store.YES, Field.Index.ANALYZED);
					valueNameField.setBoost(1F);
					doc.add(valueNameField);
					if (!TextUtils.isEmpty(value.getDescription())) {
						Field valueDescriptionField = new Field(
								FIELD_VALUE_DESCRIPTION,
								value.getDescription(), Field.Store.YES,
								Field.Index.ANALYZED);
						valueDescriptionField.setBoost(.5F);
						doc.add(valueDescriptionField);
					}

					doc
							.add(new Field(FIELD_URI, value.getURI(),
									Field.Store.YES,
									Field.Index.NOT_ANALYZED_NO_NORMS));
					writer.addDocument(doc);
				}
				monitor.increment(1);
			}
			writer.close();
			directory.close();
		} catch (Exception ex) {
			throw new RuntimeException(
					"an error occurred while indexing context model", ex);
		}
	}

	private File getIndexDirectory(Context context) {
		return context.getFileStreamPath(INDEX_DIRECTORY_NAME);
	}
}
