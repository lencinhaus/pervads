package it.polimi.dei.dbgroup.pedigree.pervads.model.matching.specialized;

import it.polimi.dei.dbgroup.pedigree.contextmodel.matching.specialized.ParameterMapping;
import it.polimi.dei.dbgroup.pedigree.contextmodel.matching.specialized.ValueMapping;
import it.polimi.dei.dbgroup.pedigree.contextmodel.matching.specialized.ValueParameterMapping;
import it.polimi.dei.dbgroup.pedigree.contextmodel.matching.specialized.ValueUriMapping;
import it.polimi.dei.dbgroup.pedigree.pervads.model.vocabulary.PervADsContextModel;

@ValueMapping(valueURIs = { PervADsContextModel.AgeBirthday,
		PervADsContextModel.AgeBirthdayInterval, PervADsContextModel.AgeExact,
		PervADsContextModel.AgeInterval })
public class AgeValue implements ScalarValue {
	@ValueUriMapping
	public String valueUri;

	@ParameterMapping(valueParameters = {
			@ValueParameterMapping(valueUri = PervADsContextModel.AgeBirthday, parameterUri = PervADsContextModel.ParAgeBirthdayX),
			@ValueParameterMapping(valueUri = PervADsContextModel.AgeExact, parameterUri = PervADsContextModel.ParAgeExactX) })
	public double exact;

	@ParameterMapping(valueParameters = {
			@ValueParameterMapping(valueUri = PervADsContextModel.AgeBirthdayInterval, parameterUri = PervADsContextModel.ParAgeBirthdayIntervalFrom),
			@ValueParameterMapping(valueUri = PervADsContextModel.AgeInterval, parameterUri = PervADsContextModel.ParAgeIntervalTo) })
	public double start;

	@ParameterMapping(valueParameters = {
			@ValueParameterMapping(valueUri = PervADsContextModel.AgeBirthdayInterval, parameterUri = PervADsContextModel.ParAgeBirthdayIntervalTo),
			@ValueParameterMapping(valueUri = PervADsContextModel.AgeInterval, parameterUri = PervADsContextModel.ParAgeIntervalFrom) })
	public double end;

	@Override
	public double getEnd() {
		return end;
	}

	@Override
	public double getScalar() {
		return exact;
	}

	@Override
	public double getStart() {
		return start;
	}

	private Type type = null;

	public Type getType() {
		if (type == null) {
			if (PervADsContextModel.AgeBirthday.equals(valueUri)
					|| PervADsContextModel.AgeExact.equals(valueUri))
				type = Type.EXACT;
			else
				type = Type.INTERVAL;
		}
		return type;
	}
}
