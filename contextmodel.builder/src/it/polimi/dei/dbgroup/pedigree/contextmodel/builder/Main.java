package it.polimi.dei.dbgroup.pedigree.contextmodel.builder;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.BuilderConfiguration.IncludedModelData;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options.CategoriesFileName;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options.Classify;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options.Complexity;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options.CreateBackwardRuleset;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options.CreateTDBStore;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options.DocTypeDeclaration;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options.Help;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options.IncludeModel;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options.ModelURI;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options.OutputFileName;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options.OutputLanguage;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options.SpecificationURI;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options.UseMetaModel;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options.Version;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options.XMLDeclaration;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Main {
	public static final Map<String, Option> options = new HashMap<String, Option>();
	public static final Set<String> VALID_LANGS = new HashSet<String>();

	static {
		Option categories = new CategoriesFileName();
		options.put("c", categories);
		options.put("-categories", categories);
		Option doctypeDeclaration = new DocTypeDeclaration();
		options.put("d", doctypeDeclaration);
		options.put("-doctype-declaration", doctypeDeclaration);
		Option help = new Help();
		options.put("h", help);
		options.put("-help", help);
		Option classify = new Classify();
		options.put("k", classify);
		options.put("-classify", classify);
		Option includeModel = new IncludeModel();
		options.put("i", includeModel);
		options.put("-include-model", includeModel);
		Option lang = new OutputLanguage();
		options.put("l", lang);
		options.put("-language", lang);
		Option meta = new UseMetaModel();
		options.put("m", meta);
		options.put("-use-meta-model", meta);
		Option output = new OutputFileName();
		options.put("o", output);
		options.put("-output", output);
		Option createBackwardRuleset = new CreateBackwardRuleset();
		options.put("r", createBackwardRuleset);
		options.put("-ruleset", createBackwardRuleset);
		Option specURI = new SpecificationURI();
		options.put("s", specURI);
		options.put("-specification-uri", specURI);
		Option modelURI = new ModelURI();
		options.put("u", modelURI);
		options.put("-model-uri", modelURI);
		Option version = new Version();
		options.put("v", version);
		options.put("-version", version);
		Option complexity = new Complexity();
		options.put("x", complexity);
		options.put("-complexity", complexity);
		Option xmlDeclaration = new XMLDeclaration();
		options.put("xd", xmlDeclaration);
		options.put("-xml-declaration", xmlDeclaration);
		Option createTDBStore = new CreateTDBStore();
		options.put("tdb", createTDBStore);
		options.put("-create-tdb-store", createTDBStore);

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
			if (config == null)
				return;
			builder = new Builder(config);
		} catch (ParseException pex) {
			System.err.println("Syntax error: " + pex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (builder != null)
			builder.run();
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
			if (!option.parse(config, args, ++current)) {
				System.out.println("Stopped.");
				return null;
			}
			current += option.getNumArguments();
		}

		// validate configuration
		// check if categories file exists
		File categoriesFolder = new File(Builder.CATEGORIES_FOLDER);
		File categoriesFile = new File(categoriesFolder, config
				.getCategoriesFileName());
		if (!categoriesFile.exists())
			throw new ParseException("categories file "
					+ config.getCategoriesFileName() + " does not exist");
		// check if valid lang
		if (!VALID_LANGS.contains(config.getOutputLanguage()))
			throw new ParseException("language " + config.getOutputLanguage()
					+ " not supported");
		// check if model uri is valid
		try {
			new URI(config.getModelURI());
		} catch (URISyntaxException urisex) {
			throw new ParseException(config.getModelURI()
					+ " is not a valid ontology URI", urisex);
		}
		if (config.getUseMetaModel()) {
			// check if specification uri is valid
			try {
				new URI(config.getSpecificationURI());
			} catch (URISyntaxException urisex) {
				throw new ParseException(config.getSpecificationURI()
						+ " is not a valid specification URI", urisex);
			}
		}

		// output configuration
		System.out
				.println("Categories file: " + config.getCategoriesFileName());
		System.out.println("Output file: " + config.getOutputFileName());
		System.out.println("Output language: " + config.getOutputLanguage());
		System.out.println("Use meta model: " + config.getUseMetaModel());
		System.out.println("Version: " + config.getVersion());
		System.out.println("Output ontology URI: " + config.getModelURI());
		if(config.getUseMetaModel()) System.out.println("Context specification URI: " + config.getSpecificationURI());
		System.out.println("Output model complexity: " + config.getComplexity().toString());
		System.out.println("Show XML declaration: " + (config.isShowXMLDeclaration()?"yes":"no"));
		System.out.println("Show DocType declaration: " + (config.isShowDocTypeDeclaration()?"yes":"no"));
		if(config.isCreateTDBStore()) {
			System.out.println("TDB store output file: " + config.getOutputFileName() + "_tdb.zip");
		}
		if(config.getIncludedModels().size() > 0) {
			System.out.println("Included models:");
			for(IncludedModelData includedModel : config.getIncludedModels()) {
				System.out.println("\t" + includedModel.path + " <" + includedModel.URI + ">");
			}
		}

		return config;
	}
}
