package it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.BuilderConfiguration;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Option;

public class UseMetaModel extends Option {
	public UseMetaModel() {
		super(0);
	}

	@Override
	public boolean parse(BuilderConfiguration config, String[] args, int offset)
			throws Exception {
		config.setUseMetaModel(true);
		return true;
	}

	@Override
	public String getDescription() {
		return "if this option is set, the generated model will include meta-model axioms. (by default id does"
				+ (BuilderConfiguration.DEFAULT_USE_META_MODEL ? "" : "n't")
				+ ")";
	}
}
