package it.polimi.dei.dbgroup.pedigree.pervads.model.matching.specialized;

import it.polimi.dei.dbgroup.pedigree.contextmodel.matching.specialized.ParameterMapping;
import it.polimi.dei.dbgroup.pedigree.contextmodel.matching.specialized.ValueMapping;
import it.polimi.dei.dbgroup.pedigree.contextmodel.matching.specialized.ValueParameterMapping;
import it.polimi.dei.dbgroup.pedigree.contextmodel.matching.specialized.ValueUriMapping;
import it.polimi.dei.dbgroup.pedigree.pervads.model.vocabulary.PervADsContextModel;

@ValueMapping(valueURIs = { PervADsContextModel.TimeExact,
		PervADsContextModel.TimeInterval })
public class TimeValue implements ScalarValue {

	@ValueUriMapping
	public String valueUri;

	@ParameterMapping(valueParameters = @ValueParameterMapping(valueUri = PervADsContextModel.TimeExact, parameterUri = PervADsContextModel.ParTimeExactDate))
	public double exact;

	@ParameterMapping(valueParameters = @ValueParameterMapping(valueUri = PervADsContextModel.TimeInterval, parameterUri = PervADsContextModel.ParTimeIntervalFrom))
	public double from;

	@ParameterMapping(valueParameters = @ValueParameterMapping(valueUri = PervADsContextModel.TimeInterval, parameterUri = PervADsContextModel.ParTimeIntervalTo))
	public double to;

	@Override
	public double getEnd() {
		return to;
	}

	@Override
	public double getScalar() {
		return exact;
	}

	@Override
	public double getStart() {
		return from;
	}

	@Override
	public Type getType() {
		if (PervADsContextModel.TimeExact.equals(valueUri))
			return Type.EXACT;
		return Type.INTERVAL;
	}

}
