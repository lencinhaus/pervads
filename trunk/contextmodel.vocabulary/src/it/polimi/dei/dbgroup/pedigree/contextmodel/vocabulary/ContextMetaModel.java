package it.polimi.dei.dbgroup.pedigree.contextmodel.vocabulary;

import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public final class ContextMetaModel {
	private static final OntModel m = ModelFactory
			.createOntologyModel(OntModelSpec.OWL_DL_MEM);

	private static final OntClass clazz(String local) {
		return m.createClass(NS + local);
	}

	private static final ObjectProperty obj(String local) {
		return m.createObjectProperty(NS + local);
	}

	private static final AnnotationProperty annot(String local) {
		return m.createAnnotationProperty(NS + local);
	}

	public static final String URI = Common.URI_BASE + "context-meta-model.owl";
	public static final String NS = URI + "#";

	// Ontologies
	public static final Ontology Ontology = m.createOntology(URI);

	// Classes
	public static final OntClass ContextSpecification = clazz("ContextSpecification");

	// Object properties
	public static final ObjectProperty contextSpecification = obj("contextSpecification");

	// Annotation properties
	public static final AnnotationProperty valueParameter = annot("valueParameter");
	public static final AnnotationProperty dimensionParameter = annot("dimensionParameter");
	public static final AnnotationProperty dimensionValue = annot("dimensionValue");
	public static final AnnotationProperty rootDimension = annot("rootDimension");
	public static final AnnotationProperty valueSubDimension = annot("valueSubDimension");
}
