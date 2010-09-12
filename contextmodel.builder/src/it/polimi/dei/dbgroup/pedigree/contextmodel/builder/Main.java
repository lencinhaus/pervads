package it.polimi.dei.dbgroup.pedigree.contextmodel.builder;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options.CategoriesFileName;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options.Help;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options.OutputFileName;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options.OutputLanguage;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options.UseMetaModel;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options.Version;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Main {
	public static final Map<String, Option> options = new HashMap<String, Option>();
	public static final Set<String> VALID_LANGS = new HashSet<String>();

	static {
		Option output = new OutputFileName();
		options.put("o", output);
		options.put("-output", output);
		Option categories = new CategoriesFileName();
		options.put("c", categories);
		options.put("-categories", categories);
		Option lang = new OutputLanguage();
		options.put("l", lang);
		options.put("-language", lang);
		Option meta = new UseMetaModel();
		options.put("m", meta);
		options.put("-use-meta-model", meta);
		Option version = new Version();
		options.put("v", version);
		options.put("-version", version);
		Option help = new Help();
		options.put("h", help);
		options.put("-help", help);
		
		VALID_LANGS.add("RDF/XML");
		VALID_LANGS.add("RDF/XML-ABBREV");
		VALID_LANGS.add("N3");
		VALID_LANGS.add("N-TRIPLE");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Context model ontology builder");
		Builder builder = null;
		try {
			BuilderConfiguration config = parseArgs(args);
			if(config == null) return;
			builder = new Builder(config);
		} catch (ParseException pex) {
			System.err.println("Syntax error: " + pex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		if(builder != null) builder.run();
	}

	private static BuilderConfiguration parseArgs(String[] args)
			throws Exception {
		BuilderConfiguration config = new BuilderConfiguration();

		// parse arguments
		int current = 0;
		while (current < args.length) {
			if (!args[current].startsWith("-"))
				throw new ParseException("options must start with - symbol: "
						+ args[current]);
			String optionName = args[current].substring(1);
			if (!options.containsKey(optionName))
				throw new ParseException("unknown option: " + optionName);
			Option option = options.get(optionName);
			if (current + option.getNumArguments() >= args.length)
				throw new ParseException("missing arguments for option "
						+ optionName);
			if(!option.parse(config, args, ++current)) {
				System.out.println("Stopped.");
				return null;
			}
			current += option.getNumArguments();
		}

		// validate configuration
		// check if categories file exists
		File categoriesFolder = new File(Builder.CATEGORIES_FOLDER);
		File categoriesFile = new File(categoriesFolder, config.getCategoriesFileName());
		if (!categoriesFile.exists())
			throw new ParseException("categories file "
					+ config.getCategoriesFileName()
					+ " does not exist");
		if(!VALID_LANGS.contains(config.getOutputLanguage())) throw new ParseException("language " + config.getOutputLanguage() + " not supported");
		
		// output configuration
		System.out.println("Categories file: "
				+ config.getCategoriesFileName());
		System.out.println("Output file: " + config.getOutputFileName());
		System.out.println("Output language: " + config.getOutputLanguage());
		System.out.println("Use meta model: " + config.getUseMetaModel());
		System.out.println("Version: " + config.getVersion());

		return config;
	}
}
