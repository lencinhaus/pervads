package it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.BuilderConfiguration;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Option;

public class Version extends Option {
	public Version() {
		super(1);
	}
	
	@Override
	public boolean parse(BuilderConfiguration config, String[] args, int offset)
			throws Exception {
		config.setVersion(args[offset]);
		return true;
	}

	@Override
	public String getDescription() {
		return "sets the output ontology version. (defaults to " + BuilderConfiguration.DEFAULT_VERSION + ")";
	}
}
