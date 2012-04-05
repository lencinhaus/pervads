package it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.BuilderConfiguration;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Option;

public class Classify extends Option {

	public Classify() {
		super(0);
	}
	
	@Override
	public String getDescription() {
		return "classifies the output model before saving it. Disabled by default";
	}

	@Override
	public boolean parse(BuilderConfiguration config, String[] args, int offset)
			throws Exception {
		config.setClassify(true);
		return true;
	}

}
