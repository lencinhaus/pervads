package it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.BuilderConfiguration;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.ModelComplexityLevel;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Option;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.ParseException;

public class Complexity extends Option {

	public Complexity() {
		super(1);
	}
	
	@Override
	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("sets the model complexity level. Must be one of [");
		boolean first = true;
		for(ModelComplexityLevel level : ModelComplexityLevel.values()) {
			if(first) first = false;
			else sb.append(", ");
			sb.append(level);
		}
		sb.append("]. (defaults to ");
		sb.append(BuilderConfiguration.DEFAULT_COMPLEXITY);
		sb.append(")");
		return sb.toString();
	}

	@Override
	public boolean parse(BuilderConfiguration config, String[] args, int offset)
			throws Exception {
		try {
			config.setComplexity(Enum.valueOf(ModelComplexityLevel.class, args[offset].toUpperCase()));
		}
		catch(IllegalArgumentException ex) {
			throw new ParseException("Unrecognized complexity level: " + args[offset], ex);
		}
		return true;
	}

}
