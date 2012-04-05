package it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.BuilderConfiguration;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Option;

public class CreateBackwardRuleset extends Option {
	public CreateBackwardRuleset() {
		super(0);
	}
	
	@Override
	public String getDescription() {
		return "creates a set of backward-only rules for Jena rule-based reasoner in outputfile.rules. Disabled by default";
	}

	@Override
	public boolean parse(BuilderConfiguration config, String[] args, int offset)
			throws Exception {
		config.setCreateBackwardRuleset(true);
		return true;
	}

}
