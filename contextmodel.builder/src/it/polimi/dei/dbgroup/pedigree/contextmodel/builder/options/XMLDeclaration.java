package it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.BuilderConfiguration;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Option;

public class XMLDeclaration extends Option {

	public XMLDeclaration() {
		super(0);
	}
	
	@Override
	public String getDescription() {
		return "shows XML declaration in the output document (if format is derived from XML). Doesn't by default";
	}

	@Override
	public boolean parse(BuilderConfiguration config, String[] args, int offset)
			throws Exception {
		config.setShowXMLDeclaration(true);
		return true;
	}

}
