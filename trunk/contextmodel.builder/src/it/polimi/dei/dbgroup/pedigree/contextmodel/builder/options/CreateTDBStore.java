package it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.BuilderConfiguration;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Option;

public class CreateTDBStore extends Option {

	public CreateTDBStore() {
		super(0);
	}
	
	@Override
	public String getDescription() {
		return "creates a TDB store loaded with the context model in outputfile_tdb.zip. Other models can be automatically included in the store, see other options. Disabled by default";
	}

	@Override
	public boolean parse(BuilderConfiguration config, String[] args, int offset)
			throws Exception {
		config.setCreateTDBStore(true);
		return true;
	}

}
