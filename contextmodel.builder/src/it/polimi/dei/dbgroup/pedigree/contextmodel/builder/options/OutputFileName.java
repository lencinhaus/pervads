package it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.BuilderConfiguration;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Option;

public class OutputFileName extends Option {

	public OutputFileName() {
		super(1);
	}
	@Override
	public void parse(BuilderConfiguration config, String[] args, int offset) {
		config.setOutputFileName(args[offset]);
	}

}
