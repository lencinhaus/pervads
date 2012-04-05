package it.polimi.dei.dbgroup.pedigree.pervads.client.android.query;

import it.polimi.dei.dbgroup.pedigree.contextmodel.matching.DimensionAssignmentMatching;
import it.polimi.dei.dbgroup.pedigree.contextmodel.matching.Matching;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ActualDimensionAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextModelFactory;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextModelProxy;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.FormalDimensionAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.context.ContextProxyManager;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.data.AttachedMediaManager;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.semantics.OWLSpecializedReasoner;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.semantics.TDBAwareOntDocumentManager;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.semantics.TDBModelManager;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Initializable;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.Logger;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.util.ProgressMonitor;
import it.polimi.dei.dbgroup.pedigree.pervads.model.matching.PervADsMatcher;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.Offer;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervAD;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervADsModelFactory;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervADsModelProxy;
import it.polimi.dei.dbgroup.pedigree.pervads.model.vocabulary.PervADsModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;

public class ResultBuilder implements Initializable {
	private static ResultBuilder instance = null;
	private final Logger L = new Logger(ResultBuilder.class.getSimpleName());
	private Map<String, List<FormalDimensionAssignment>> queryDefinitions;
	private Map<String, QueryResult> resultsMap;
	private QueryManager queryManager = null;
	private PervADsMatcher<FormalDimensionAssignment, ActualDimensionAssignment> matcher;
	private Reasoner reasoner;
	private boolean initialized = false;
	private boolean started = false;

	private ResultBuilder() {
	}

	public static ResultBuilder getInstance() {
		if (instance == null)
			instance = new ResultBuilder();
		return instance;
	}

	public boolean processContent(InputStream stream,
			AttachedMediaManager mediaManager) {
		checkStarted();

		try {
			if (Logger.D)
				L.d("parsing content from stream");
			Model model = ModelFactory.createDefaultModel();
			model.read(stream, null);
			InfModel inferenceModel = ModelFactory.createInfModel(reasoner,
					model);
			PervADsModelProxy proxy = PervADsModelFactory.createProxy(
					ContextProxyManager.getInstance().getProxy(),
					inferenceModel);

			if (Logger.D)
				L.d("retrieving local attached media");
			Map<String, String> cachedLocalMedia = new HashMap<String, String>();
			Set<String> unreachableLocalMedia = new HashSet<String>();
			for (PervAD pervad : proxy.listPervADs()) {
				for (Offer offer : pervad.listOffers()) {
					for (String media : offer.getAttachedMedia()) {
						if (!cachedLocalMedia.containsKey(media)
								&& !unreachableLocalMedia.contains(media)) {
							// try to cache it
							try {
								String cached = mediaManager
										.cacheIfNeeded(media);
								if (cached == null)
									unreachableLocalMedia.add(media);
								else if (!cached.equals(media))
									cachedLocalMedia.put(media, cached);
							} catch (IOException ex) {
								if (Logger.W)
									L
											.w(
													"An error occurred while trying to cache local attached media",
													ex);
							}
						}
					}
				}
			}

			for (PervAD pervad : proxy.listPervADs()) {
				if (Logger.D)
					L.d("processing pervad " + pervad.getURI());

				matcher.setSource(pervad.getContext());
				for (String queryName : queryDefinitions.keySet()) {
					if (Logger.D)
						L.d("matching pervad with query " + queryName);
					QueryResult result = resultsMap.get(queryName);
					matcher.setSourceDimensionAssignments(queryDefinitions
							.get(queryName));
					Matching<FormalDimensionAssignment, ActualDimensionAssignment> matching = matcher
							.match();
					if (matching.getScore() > 0) {
						if (Logger.D)
							L.d("positive matching with score "
									+ matching.getScore());
						PervADProxy lPervad = new PervADProxy();
						List<MatchingAssignment> lAssignmentMatchings = new ArrayList<MatchingAssignment>();
						for (DimensionAssignmentMatching<FormalDimensionAssignment, ActualDimensionAssignment> assignmentMatching : matching
								.getMatchings()) {
							AssignmentProxy lAssignment = new AssignmentProxy(
									assignmentMatching.getTarget().getValue()
											.getURI());
							MatchingAssignment lAssignmentMatching = new MatchingAssignment(
									assignmentMatching.getScore(), lAssignment);
							lAssignmentMatchings.add(lAssignmentMatching);
						}
						MatchingPervAD matchingPervad = new MatchingPervAD(
								matching.getScore(), lAssignmentMatchings,
								lPervad);
						result.getMatchingPervADs().add(matchingPervad);
					} else if (Logger.D)
						L.d("no matching");
				}

				if (Logger.D)
					L.d("done processing pervad " + pervad.getURI());
			}
			return true;
		} catch (Exception ex) {
			L.w("An exception was thrown while processing content", ex);
			return false;
		}
	}

	public boolean start() {
		checkInitialized();

		if (started) {
			stop();
		}

		List<Query> queries = getUpdatedQueries();

		if (queries.size() > 0) {
			ContextModelProxy contextModel = ContextProxyManager.getInstance()
					.getProxy();
			queryDefinitions = new HashMap<String, List<FormalDimensionAssignment>>();
			resultsMap = new HashMap<String, QueryResult>();
			for (Query query : queries) {
				if (query.isEnabled()) {
					started = true;

					String queryName = query.getName();
					if (Logger.D)
						L.d("initializing assignment definitions for query "
								+ queryName);
					List<FormalDimensionAssignment> definitions = new ArrayList<FormalDimensionAssignment>();
					for (AssignmentProxy assignment : query
							.getAssignments()) {
						Value value = contextModel.findValue(assignment
								.getValueURI());
						definitions.add(ContextModelFactory
								.createDimensionAssignment(value));
					}
					queryDefinitions.put(queryName, definitions);
					QueryResult result = new QueryResult(queryName);
					resultsMap.put(queryName, result);
				}
			}
		}

		return started;
	}

	public List<QueryResult> stop() {
		checkStarted();

		List<QueryResult> results = new ArrayList<QueryResult>(resultsMap
				.values());
		queryDefinitions = null;
		resultsMap = null;
		started = false;
		return results;
	}

	public void initialize(Context context, ProgressMonitor monitor) {
		if (!initialized) {
			// initialize dependent initializables
			TDBModelManager tdbManager = TDBModelManager.getInstance();
			if (!tdbManager.isInitialized(context))
				tdbManager.initialize(context, monitor);
			ContextProxyManager contextManager = ContextProxyManager
					.getInstance();
			if (!contextManager.isInitialized(context))
				contextManager.initialize(context, monitor);

			// create reasoner
			if (Logger.D)
				L.d("creating reasoner schema with pervads model");
			OntDocumentManager docManager = TDBAwareOntDocumentManager
					.getInstance();
			Model pervadsModel = docManager.getModel(PervADsModel.URI);
			OntModelSpec schemaSpec = new OntModelSpec(OntModelSpec.OWL_MEM);
			schemaSpec.setDocumentManager(docManager);
			OntModel schema = ModelFactory.createOntologyModel(schemaSpec,
					pervadsModel);
			try {
				if (Logger.D)
					L
							.d("creating specialized owl reasoner and binding it to schema");
				reasoner = new OWLSpecializedReasoner(context)
						.bindSchema(schema);
			} catch (IOException ex) {
				throw new RuntimeException("cannot create reasoner", ex);
			}

			// create query manager
			queryManager = new QueryManager(context);
			
			// create the location service and matcher
			AndroidLocationService locationService = new AndroidLocationService(context);
			matcher = new PervADsMatcher<FormalDimensionAssignment, ActualDimensionAssignment>(locationService);

			initialized = true;
		}
	}

	@Override
	public boolean isInitialized(Context context) {
		return initialized;
	}

	public boolean isStarted() {
		return started;
	}

	private List<Query> getUpdatedQueries() {
		queryManager.synchronize();
		return queryManager.getQueries();
	}

	private void checkStarted() {
		checkInitialized();
		if (!started)
			throw new IllegalStateException(
					"ResultBuilder must be started before calling this method");
	}

	private void checkInitialized() {
		if (!initialized)
			throw new IllegalStateException(
					"ResultBuilder must be initialized before calling this method");
	}
}
