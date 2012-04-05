package it.polimi.dei.dbgroup.pedigree.contextmodel.vocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public final class ContextModelVocabulary {
	private static final Model m = ModelFactory.createDefaultModel();

	private static final Resource resource(String local) {
		if (local == null)
			return m.createResource(URI);
		return m.createResource(NS + local);
	}

	private static final Property property(String local) {
		return m.createProperty(NS + local);
	}

	public static final String URI = Common.URI_BASE + "context-model-vocabulary.owl";
	public static final String NS = URI + "#";

	// Ontologies
	public static final Resource Ontology = resource(null);

	// Classes
	public static final Resource Context = resource("Context");
	public static final Resource Value = resource("Value");
	public static final Resource Dimension = resource("Dimension");
	public static final Resource Parameter = resource("Parameter");
	public static final Resource ActualDimension = resource("ActualDimension");
	public static final Resource ActualParameter = resource("ActualParameter");

	// Object properties
	public static final Property formalDimension = property("formalDimension");
	public static final Property hasParameter = property("hasParameter");
	public static final Property parameterOfValue = property("parameterOfValue");
	public static final Property hasDimension = property("hasDimension");
	public static final Property inContext = property("inContext");
	public static final Property dimensionValue = property("dimensionValue");
	public static final Property formalParameter = property("formalParameter");
	public static final Property valueOfDimension = property("valueOfDimension");

	// Datatype properties
	public static final Property parameterValue = property("parameterValue");
}
