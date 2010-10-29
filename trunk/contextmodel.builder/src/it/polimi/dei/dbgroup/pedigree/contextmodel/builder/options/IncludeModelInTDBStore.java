package it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.BuilderConfiguration;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Option;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.BuilderConfiguration.IncludedModelData;

public class IncludeModelInTDBStore extends Option {
	public IncludeModelInTDBStore() {
		super(2);
	}
	
	@Override
	public String getDescription() {
		return "adds a model to be included in the created TDB store (TDB store creation option must be enabled). First parameter is the url of the model file, second parameter is its base URI.";
	}

	@Override
	public boolean parse(BuilderConfiguration config, String[] args, int offset)
			throws Exception {
		config.getTDBStoreIncludedModels().add(new IncludedModelData(args[offset], args[offset+1]));
		return true;
	}

}
