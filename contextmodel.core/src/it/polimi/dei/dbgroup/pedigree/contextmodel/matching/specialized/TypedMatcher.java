package it.polimi.dei.dbgroup.pedigree.contextmodel.matching.specialized;

import it.polimi.dei.dbgroup.pedigree.contextmodel.matching.IncompatibleAssignmentsException;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.DimensionAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ParameterAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ValueParameter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class TypedMatcher<S extends DimensionAssignment<? extends ParameterAssignment>, T extends DimensionAssignment<? extends ParameterAssignment>, V>
		extends SpecializedMatcher<S, T> {
	private static final class ParameterDefinition {
		public Field field;
		public boolean optional;

		public ParameterDefinition(Field field, boolean optional) {
			this.field = field;
			this.optional = optional;
		}
	}

	private Class<V> valueClass;
	private Constructor<V> valueConstructor;
	private Field valueUriField = null;
	private final Map<String, Map<String, ParameterDefinition>> parameterMap = new HashMap<String, Map<String, ParameterDefinition>>();

	public TypedMatcher(Class<V> valueClass) {
		this.valueClass = valueClass;
		analyzeValueClass();
	}

	private void analyzeValueClass() {
		// find a suitable constructor
		try {
			valueConstructor = valueClass.getConstructor();
		} catch (NoSuchMethodException ex) {
			throw new IllegalArgumentException("valueClass " + valueClass
					+ " does not specify a public parameterless constructor",
					ex);
		}

		// read value mappings and initalize compatibility checker
		ValueMapping valueMapping = valueClass
				.getAnnotation(ValueMapping.class);
		if (valueMapping == null)
			throw new IllegalArgumentException("valueClass " + valueClass
					+ " is not annotated with " + ValueMapping.class.getName());
		ValueBasedCompatibilityChecker checker = new ValueBasedCompatibilityChecker();
		for (String valueURI : valueMapping.valueURIs()) {
			checker.addCompatibleValue(valueURI);
		}
		setCompatibilityChecker(checker);

		// read parameter mappings and initialize parameter map
		for (Field field : valueClass.getFields()) {
			ParameterMapping parameterMapping = field
					.getAnnotation(ParameterMapping.class);
			if (parameterMapping != null) {
				for (ValueParameterMapping valueParameterMapping : parameterMapping
						.valueParameters()) {
					Map<String, ParameterDefinition> definitions = parameterMap
							.get(valueParameterMapping.valueUri());
					if (definitions == null) {
						definitions = new HashMap<String, ParameterDefinition>();
						parameterMap.put(valueParameterMapping.valueUri(),
								definitions);
					}
					ParameterDefinition definition = new ParameterDefinition(
							field, valueParameterMapping.optional());
					definitions.put(valueParameterMapping.parameterUri(),
							definition);
				}
			}
			else {
				// check value uri mapping
				ValueUriMapping valueUriMapping = field.getAnnotation(ValueUriMapping.class);
				if(valueUriMapping != null) {
					// check field type
					if(!String.class.isAssignableFrom(field.getType())) throw new IllegalArgumentException("field " + field + " of valueClass " + valueClass + " is annotated with " + ValueUriMapping.class.getName() + " but its type is not assignable to String");
					if(valueUriField == null) valueUriField = field;
					else throw new IllegalArgumentException("more than one field of valueClass " + valueClass + " is annotated with " + ValueUriMapping.class.getName());
				}
			}
		}
	}

	@Override
	public double match(S sourceAssignment, T targetAssignment)
			throws IncompatibleAssignmentsException {
		V sourceValue = parseAssignment(sourceAssignment);
		V targetValue = parseAssignment(targetAssignment);

		return matchValues(sourceValue, targetValue);
	}

	private V parseAssignment(
			DimensionAssignment<? extends ParameterAssignment> assignment) {
		V typedValue;
		try {
			typedValue = valueConstructor.newInstance();
		} catch (Exception ex) {
			throw new RuntimeException(
					"cannot create an instance of valueClass " + valueClass, ex);
		}

		String valueUri = assignment.getValue().getURI();
		
		if(valueUriField != null) {
			try {
				valueUriField.set(typedValue, valueUri);
			}
			catch(Exception ex) {
				throw new RuntimeException("cannot assign value Uri to field " + valueUriField, ex);
			}
		}

		Map<String, ParameterDefinition> definitions = parameterMap
				.get(valueUri);

		if (definitions != null) {
			// initialize unassigned mandatory parameter set
			Set<String> unassignedMandatoryParameters = new HashSet<String>();
			for (String parameterURI : definitions.keySet()) {
				ParameterDefinition definition = definitions.get(parameterURI);
				if (!definition.optional)
					unassignedMandatoryParameters.add(parameterURI);
			}

			for (ParameterAssignment parameterAssignment : assignment
					.listParameterAssignments()) {
				String parameterURI = parameterAssignment.getParameter()
						.getURI();
				if (definitions.containsKey(parameterURI)) {
					ParameterDefinition definition = definitions
							.get(parameterURI);
					Object parameterValue = convertParameterValue(parameterAssignment.getParameter(), parameterAssignment.getValue());
					try {
						definition.field.set(typedValue, parameterValue);
					} catch (Exception ex) {
						throw new RuntimeException("cannot assign value "
								+ parameterValue + " of parameter "
								+ parameterAssignment.getParameter() + " to field " + definition.field, ex);
					}

					unassignedMandatoryParameters.remove(parameterURI);
				}
			}

			if (!unassignedMandatoryParameters.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				for (String parameterURI : unassignedMandatoryParameters) {
					if (sb.length() > 0)
						sb.append(", ");
					sb.append(parameterURI);
				}
				throw new RuntimeException(
						"The following non-optional parameters were not assigned: "
								+ sb.toString());
			}
		}

		return typedValue;
	}

	protected abstract double matchValues(V sourceValue, V targetValue);
	
	protected Object convertParameterValue(ValueParameter parameter, Object value) {
		return value;
	}
}
