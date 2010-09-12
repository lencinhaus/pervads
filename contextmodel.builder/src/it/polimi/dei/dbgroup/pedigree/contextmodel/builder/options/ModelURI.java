package it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.BuilderConfiguration;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Option;

public class ModelURI extends Option {
	public ModelURI() {
		super(1);
	}

	@Override
	public String getDescription() {
		return "sets the output ontology URI. (defaults to "
				+ BuilderConfiguration.DEFAULT_MODEL_URI + ")";
	}

	@Override
	public boolean parse(BuilderConfiguration config, String[] args, int offset)
			throws Exception {
		config.setModelURI(args[offset]);
		return true;
	}

}
