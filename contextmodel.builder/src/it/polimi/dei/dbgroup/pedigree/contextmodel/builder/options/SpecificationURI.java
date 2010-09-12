package it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.BuilderConfiguration;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Option;

public class SpecificationURI extends Option {

	public SpecificationURI() {
		super(1);
	}
	@Override
	public String getDescription() {
		return "if meta model is used, sets the context specification uri. (defaults to " + BuilderConfiguration.DEFAULT_SPECIFICATION_URI + ")";
	}

	@Override
	public boolean parse(BuilderConfiguration config, String[] args, int offset)
			throws Exception {
		config.setSpecificationURI(args[offset]);
		return true;
	}

}
