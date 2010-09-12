package it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Builder;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.BuilderConfiguration;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Option;

public class CategoriesFileName extends Option {
	public CategoriesFileName() {
		super(1);
	}
	
	@Override
	public boolean parse(BuilderConfiguration config, String[] args, int offset) {
		config.setCategoriesFileName(args[offset]);
		return true;
	}

	@Override
	public String getDescription() {
		return "sets the categories main source filename inside the "
				+ Builder.CATEGORIES_FOLDER + " folder. (defaults to "
				+ BuilderConfiguration.DEFAULT_CATEGORIES_FILE_NAME + ")";
	}

	
}
