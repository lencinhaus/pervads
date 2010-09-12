package it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.BuilderConfiguration;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Option;

public class Version extends Option {
	public Version() {
		super(1);
	}
	
	@Override
	public void parse(BuilderConfiguration config, String[] args, int offset)
			throws Exception {
		config.setVersion(args[offset]);
	}

}
