package it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Builder;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.BuilderConfiguration;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Option;

public class OutputFileName extends Option {

	public OutputFileName() {
		super(1);
	}
	@Override
	public boolean parse(BuilderConfiguration config, String[] args, int offset) {
		config.setOutputFileName(args[offset]);
		return true;
	}

	@Override
	public String getDescription() {
		return "sets the output model filename (without extension) inside the "
				+ Builder.MODELS_FOLDER + " folder. (defaults to "
				+ BuilderConfiguration.DEFAULT_OUTPUT_FILE_NAME + ")";
	}
}
