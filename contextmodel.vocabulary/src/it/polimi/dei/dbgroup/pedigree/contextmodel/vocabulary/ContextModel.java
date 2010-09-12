package it.polimi.dei.dbgroup.pedigree.contextmodel.vocabulary;

public final class ContextModel {
	public static final String URI = Common.URI_BASE + "context-model.owl";
	public static final String NS = URI + "#";
	
	// Classes
	public static final String CONTEXT_CLASS_URI = NS + "Context";
	public static final String DIMENSION_VALUE_CLASS_URI = NS + "DimensionValue";
	public static final String FORMAL_DIMENSION_CLASS_URI = NS + "FormalDimension";
	public static final String FORMAL_PARAMETER_CLASS_URI = NS + "FormalParameter";
	public static final String DIMENSION_ASSIGNMENT_CLASS_URI = NS + "DimensionAssignment";
	public static final String PARAMETER_ASSIGNMENT_CLASS_URI = NS + "ParameterAssignment";
	
	// Object properties
	public static final String ASSIGNMENT_DIMENSION_PROPERTY_URI = NS + "assignmentDimension";
	public static final String ASSIGNMENT_PARAMETER_PROPERTY_URI = NS + "assignmentParameter";
	public static final String CONTEXT_SPECIFICATION_PROPERTY_URI = NS + "contextSpecification";
	public static final String DIMENSION_ASSIGNMENT_PROPERTY_URI = NS + "dimensionAssignment";
	public static final String DIMENSION_ASSIGNMENT_VALUE_PROPERTY_URI = NS + "dimensionAssignmentValue";
	public static final String PARAMETER_ASSIGNMENT_PROPERTY_URI = NS + "parameterAssignment";
	
	// Datatype properties
	public static final String PARAMETER_ASSIGNMENT_VALUE_PROPERTY_URI = NS + "parameterAssignmentValue";
}
