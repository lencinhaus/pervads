package it.polimi.dei.dbgroup.pedigree.contextmodel.vocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public final class ContextModel {
	private static final Model m = ModelFactory.createDefaultModel();

	private static final Resource resource(String local) {
		if (local == null)
			return m.createResource(URI);
		return m.createResource(NS + local);
	}

	private static final Property property(String local) {
		return m.createProperty(NS + local);
	}

	public static final String URI = Common.URI_BASE + "context-model.owl";
	public static final String NS = URI + "#";

	// Ontologies
	public static final Resource Ontology = resource(null);

	// Classes
	public static final Resource Context = resource("Context");
	public static final Resource DimensionValue = resource("DimensionValue");
	public static final Resource FormalDimension = resource("FormalDimension");
	public static final Resource FormalParameter = resource("FormalParameter");
	public static final Resource DimensionAssignment = resource("DimensionAssignment");
	public static final Resource ParameterAssignment = resource("ParameterAssignment");

	// Object properties
	public static final Property assignmentDimension = property("assignmentDimension");
	public static final Property assignmentParameter = property("assignmentParameter");
	public static final Property dimensionAssignment = property("dimensionAssignment");
	public static final Property dimensionAssignmentValue = property("dimensionAssignmentValue");
	public static final Property parameterAssignment = property("parameterAssignment");

	// Datatype properties
	public static final Property parameterAssignmentValue = property("parameterAssignmentValue");
}
