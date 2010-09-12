package it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.BuilderConfiguration;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Option;

public class UseMetaModel extends Option {
	public UseMetaModel()
	{
		super(0);
	}
	
	@Override
	public void parse(BuilderConfiguration config, String[] args, int offset)
			throws Exception {
		config.setUseMetaModel(true);
	}

}
