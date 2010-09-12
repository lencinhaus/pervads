package it.polimi.dei.dbgroup.pedigree.contextmodel.builder;

import it.polimi.dei.dbgroup.pedigree.contextmodel.vocabulary.ContextMetaModel;
import it.polimi.dei.dbgroup.pedigree.contextmodel.vocabulary.ContextModel;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class ResourceProxy {
	private OntModel contextModel;
	private OntModel contextMetaModel;
	private Map<String, OntResource> resourceMap = new HashMap<String, OntResource>();

	public ResourceProxy(boolean useMetaModel) {
		// initialize ontology doc manager
		OntDocumentManager dm = OntDocumentManager.getInstance();
		dm.addAltEntry(ContextModel.URI, "file:owl/context-model.owl");
		if (useMetaModel)
			dm.addAltEntry(ContextMetaModel.URI,
					"file:owl/context-meta-model.owl");

		// read context model
		this.contextModel = ModelFactory
				.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		this.contextModel.read(ContextModel.URI);

		if (useMetaModel) {
			// read context meta model
			this.contextMetaModel = ModelFactory
					.createOntologyModel(OntModelSpec.OWL_DL_MEM);
			this.contextMetaModel.read(ContextMetaModel.URI);
		}
	}

	public Ontology getContextModelOntology() throws Exception {
		return getOntology(contextModel, ContextModel.URI);
	}

	public Ontology getContextMetaModelOntology() throws Exception {
		return getOntology(contextMetaModel, ContextMetaModel.URI);
	}

	public OntClass getFormalDimensionClass() throws Exception {
		return getClass(contextModel, ContextModel.FORMAL_DIMENSION_CLASS_URI);
	}

	public OntClass getDimensionValueClass() throws Exception {
		return getClass(contextModel, ContextModel.DIMENSION_VALUE_CLASS_URI);
	}

	public OntClass getDimensionAssignmentClass() throws Exception {
		return getClass(contextModel,
				ContextModel.DIMENSION_ASSIGNMENT_CLASS_URI);
	}

	public OntClass getFormalParameterClass() throws Exception {
		return getClass(contextModel, ContextModel.FORMAL_PARAMETER_CLASS_URI);
	}

	public OntProperty getAssignmentDimensionProperty() throws Exception {
		return getProperty(contextModel,
				ContextModel.ASSIGNMENT_DIMENSION_PROPERTY_URI);
	}

	public OntProperty getDimensionAssignmentValueProperty() throws Exception {
		return getProperty(contextModel,
				ContextModel.DIMENSION_ASSIGNMENT_VALUE_PROPERTY_URI);
	}

	public OntProperty getRootDimensionProperty() throws Exception {
		return getProperty(contextMetaModel,
				ContextMetaModel.ROOT_DIMENSION_PROPERTY_URI);
	}

	public OntProperty getValueSubDimensionProperty() throws Exception {
		return getProperty(contextMetaModel,
				ContextMetaModel.VALUE_SUB_DIMENSION_PROPERTY_URI);
	}

	public OntProperty getDimensionValueProperty() throws Exception {
		return getProperty(contextMetaModel,
				ContextMetaModel.DIMENSION_VALUE_PROPERTY_URI);
	}

	public OntProperty getDimensionParameterProperty() throws Exception {
		return getProperty(contextMetaModel,
				ContextMetaModel.DIMENSION_PARAMETER_PROPERTY_URI);
	}

	public OntProperty getValueParameterProperty() throws Exception {
		return getProperty(contextMetaModel,
				ContextMetaModel.VALUE_PARAMETER_PROPERTY_URI);
	}

	public OntClass getContextSpecificationClass() throws Exception {
		return getClass(contextMetaModel,
				ContextMetaModel.CONTEXT_SPECIFICATION_CLASS_URI);
	}

	protected Ontology getOntology(OntModel model, String ontologyURI)
			throws Exception {
		Ontology ont = (Ontology) resourceMap.get(ontologyURI);
		if (ont == null) {
			ont = checkOntology(model, ontologyURI);
			resourceMap.put(ontologyURI, ont);
		}
		return ont;
	}

	protected OntClass getClass(OntModel model, String classURI)
			throws Exception {
		OntClass cls = (OntClass) resourceMap.get(classURI);
		if (cls == null) {
			cls = checkClass(model, classURI);
			resourceMap.put(classURI, cls);
		}
		return cls;
	}

	protected OntProperty getProperty(OntModel model, String propertyURI)
			throws Exception {
		OntProperty prop = (OntProperty) resourceMap.get(propertyURI);
		if (prop == null) {
			prop = checkProperty(model, propertyURI);
			resourceMap.put(propertyURI, prop);
		}
		return prop;
	}

	protected Ontology checkOntology(OntModel model, String ontologyURI)
			throws Exception {
		Ontology ont = model.getOntology(ontologyURI);
		if (ont == null)
			throw new Exception("Cannot find ontology " + ontologyURI
					+ " in context model");
		return ont;
	}

	protected OntClass checkClass(OntModel model, String classURI)
			throws Exception {
		OntClass cls = model.getOntClass(classURI);
		if (cls == null)
			throw new Exception("Cannot find class " + classURI
					+ " in context model");
		return cls;
	}

	protected OntProperty checkProperty(OntModel model, String propertyURI)
			throws Exception {
		OntProperty prop = model.getOntProperty(propertyURI);
		if (prop == null)
			throw new RuntimeException("Cannot find property " + propertyURI
					+ " in context model");
		return prop;
	}
}
