package it.polimi.dei.dbgroup.pedigree.contextmodel.vocabulary;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public final class ContextModel {
	private static final OntModel m = ModelFactory
			.createOntologyModel(OntModelSpec.OWL_DL_MEM);

	private static final OntClass clazz(String local) {
		return m.createClass(NS + local);
	}

	private static final ObjectProperty obj(String local) {
		return m.createObjectProperty(NS + local);
	}

	private static final DatatypeProperty data(String local) {
		return m.createDatatypeProperty(NS + local);
	}

	public static final String URI = Common.URI_BASE + "context-model.owl";
	public static final String NS = URI + "#";

	// Ontologies
	public static final Ontology Ontology = m.createOntology(URI);

	// Classes
	public static final OntClass Context = clazz("Context");
	public static final OntClass DimensionValue = clazz("DimensionValue");
	public static final OntClass FormalDimension = clazz("FormalDimension");
	public static final OntClass FormalParameter = clazz("FormalParameter");
	public static final OntClass DimensionAssignment = clazz("DimensionAssignment");
	public static final OntClass ParameterAssignment = clazz("ParameterAssignment");

	// Object properties
	public static final ObjectProperty assignmentDimension = obj("assignmentDimension");
	public static final ObjectProperty assignmentParameter = obj("assignmentParameter");
	public static final ObjectProperty dimensionAssignment = obj("dimensionAssignment");
	public static final ObjectProperty dimensionAssignmentValue = obj("dimensionAssignmentValue");
	public static final ObjectProperty parameterAssignment = obj("parameterAssignment");

	// Datatype properties
	public static final DatatypeProperty parameterAssignmentValue = data("parameterAssignmentValue");
}
