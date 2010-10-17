package it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.BuilderConfiguration;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Option;

public class DocTypeDeclaration extends Option {

	public DocTypeDeclaration() {
		super(0);
	}
	
	@Override
	public String getDescription() {
		return "shows DOCTYPE declaration in the output document (if format is derived from XML). Doesn't by default";
	}

	@Override
	public boolean parse(BuilderConfiguration config, String[] args, int offset)
			throws Exception {
		config.setShowDocTypeDeclaration(true);
		return true;
	}

}
