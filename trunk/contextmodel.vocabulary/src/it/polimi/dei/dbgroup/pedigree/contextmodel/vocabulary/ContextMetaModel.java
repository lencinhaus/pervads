package it.polimi.dei.dbgroup.pedigree.contextmodel.vocabulary;

public final class ContextMetaModel {
	public static final String URI = Common.URI_BASE + "context-meta-model.owl";
	public static final String NS = URI + "#";
	
	// Classes
	public static final String CONTEXT_SPECIFICATION_CLASS_URI = NS + "ContextSpecification";
	
	// Annotation properties
	public static final String VALUE_PARAMETER_PROPERTY_URI = NS + "valueParameter";
	public static final String DIMENSION_PARAMETER_PROPERTY_URI = NS + "dimensionParameter";
	public static final String DIMENSION_VALUE_PROPERTY_URI = NS + "dimensionValue";
	public static final String ROOT_DIMENSION_PROPERTY_URI = NS + "rootDimension";
	public static final String VALUE_SUB_DIMENSION_PROPERTY_URI = NS + "valueSubDimension";
}
