package it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.BuilderConfiguration;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Main;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Option;

public class OutputLanguage extends Option {

	public OutputLanguage() {
		super(1);
	}

	@Override
	public boolean parse(BuilderConfiguration config, String[] args, int offset)
			throws Exception {
		String lang = args[offset];
		config.setOutputLanguage(lang);
		return true;
	}

	@Override
	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("sets the output model serialization language. Valid values for this option are ");
		boolean firstLang = true;
		for(String lang : Main.VALID_LANGS) {
			if(firstLang) firstLang = false;
			else sb.append(", ");
			sb.append(lang);
		}
		sb.append(". (defaults to " + BuilderConfiguration.DEFAULT_OUTPUT_LANGUAGE + ")");
		return sb.toString();
	}
}
