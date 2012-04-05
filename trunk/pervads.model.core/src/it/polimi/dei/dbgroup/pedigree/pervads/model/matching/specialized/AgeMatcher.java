package it.polimi.dei.dbgroup.pedigree.pervads.model.matching.specialized;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.DimensionAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ParameterAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ValueParameter;

import java.util.Calendar;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;

public class AgeMatcher<S extends DimensionAssignment<? extends ParameterAssignment>, T extends DimensionAssignment<? extends ParameterAssignment>>
		extends ScalarMatcher<S, T, AgeValue> {

	public AgeMatcher() {
		super(AgeValue.class);
	}

	@Override
	protected double convertParameterToScalar(ValueParameter parameter,
			Object value) {
		Calendar c = null;
		if (value instanceof XSDDateTime) {
			// value expressed as a birthdate
			c = ((XSDDateTime) value).asCalendar();
		}
		else if (value instanceof Integer) {
			// value expressed as age, convert to birthdate
			int age = (Integer) value;
			c = Calendar.getInstance();
			c.add(Calendar.YEAR, -age);
		}
		
		if(c != null) {
			return new Long(c.getTimeInMillis()).doubleValue();
		}
		
		return super.convertParameterToScalar(parameter, value);
	}
}
