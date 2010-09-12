package it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.BuilderConfiguration;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Option;

public class CategoriesFileName extends Option {
	public CategoriesFileName() {
		super(1);
	}
	
	@Override
	public void parse(BuilderConfiguration config, String[] args, int offset) {
		config.setCategoriesFileName(args[offset]);
	}

}
