package it.polimi.dei.dbgroup.pedigree.contextmodel.vocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public final class ContextMetaModel {
	private static final Model m = ModelFactory.createDefaultModel();

	private static final Resource resource(String local) {
		if (local == null)
			return m.createResource(URI);
		return m.createResource(NS + local);
	}

	private static final Property property(String local) {
		return m.createProperty(NS + local);
	}

	public static final String URI = Common.URI_BASE + "context-meta-model.owl";
	public static final String NS = URI + "#";

	// Ontologies
	public static final Resource Ontology = resource(null);

	// Classes
	public static final Resource ContextSpecification = resource("ContextSpecification");

	// Object properties
	public static final Property contextSpecification = property("contextSpecification");

	// Annotation properties
	public static final Property valueParameter = property("valueParameter");
	public static final Property dimensionParameter = property("dimensionParameter");
	public static final Property dimensionValue = property("dimensionValue");
	public static final Property rootDimension = property("rootDimension");
	public static final Property valueSubDimension = property("valueSubDimension");
}
