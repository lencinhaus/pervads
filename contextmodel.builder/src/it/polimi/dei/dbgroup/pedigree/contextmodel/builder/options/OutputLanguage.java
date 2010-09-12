package it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.BuilderConfiguration;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Option;

public class OutputLanguage extends Option {

	public OutputLanguage() {
		super(1);
	}

	@Override
	public void parse(BuilderConfiguration config, String[] args, int offset)
			throws Exception {
		String lang = args[offset];
		config.setOutputLanguage(lang);
	}

}
