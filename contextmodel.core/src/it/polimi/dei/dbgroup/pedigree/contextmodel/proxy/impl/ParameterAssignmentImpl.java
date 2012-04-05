package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ParameterAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ValueParameter;

public class ParameterAssignmentImpl implements ParameterAssignment {
	protected ValueParameter parameter;
	protected Object value;

	public ParameterAssignmentImpl(ValueParameter parameter, Object value) {
		this.parameter = parameter;
		checkValue(value);
		this.value = value;
	}

	@Override
	public ValueParameter getParameter() {
		return parameter;
	}

	@Override
	public Object getValue() {
		return value;
	}

	protected void checkValue(Object value) {
		if(!parameter.getValuesType().isValidValue(value)) throw new IllegalArgumentException("value " + value + " is not valid for parameter " + parameter + ", expected data type is " + parameter.getValuesType());
	}

	@Override
	public boolean equals(Object arg0) {
		if(arg0 instanceof ParameterAssignment) {
			ParameterAssignment p = (ParameterAssignment) arg0;
			return value.equals(p.getValue()) && parameter.equals(p.getParameter());
		}
		return super.equals(arg0);
	}
	
	
}
