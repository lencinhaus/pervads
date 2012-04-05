package it.polimi.dei.dbgroup.pedigree.pervads.model.matching.specialized;

import java.util.Calendar;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.DimensionAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ParameterAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ValueParameter;

public class TimeMatcher<S extends DimensionAssignment<? extends ParameterAssignment>, T extends DimensionAssignment<? extends ParameterAssignment>> extends ScalarMatcher<S, T, TimeValue> {
	public TimeMatcher() {
		super(TimeValue.class);
	}
	
	@Override
	protected double convertParameterToScalar(ValueParameter parameter,
			Object value) {
		if (value instanceof XSDDateTime) {
			// value expressed as a date
			Calendar c = ((XSDDateTime) value).asCalendar();
			return new Long(c.getTimeInMillis()).doubleValue();
		}
		return super.convertParameterToScalar(parameter, value);
	}
	
}
