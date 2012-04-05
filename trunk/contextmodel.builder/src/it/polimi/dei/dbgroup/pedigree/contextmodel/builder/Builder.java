package it.polimi.dei.dbgroup.pedigree.contextmodel.builder;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.BuilderConfiguration.IncludedModelData;
import it.polimi.dei.dbgroup.pedigree.contextmodel.vocabulary.ContextMetaModel;
import it.polimi.dei.dbgroup.pedigree.contextmodel.vocabulary.ContextModelVocabulary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.WordUtils;
import org.apache.xerces.parsers.SAXParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.rulesys.BuiltinRegistry;
import com.hp.hpl.jena.reasoner.rulesys.FBRuleInfGraph;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.shared.UnknownPropertyException;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.TDBLoader;
import com.hp.hpl.jena.tdb.solver.stats.StatsCollector;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.OWL;

import de.fuberlin.wiwiss.ng4j.NamedGraph;
import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
import de.fuberlin.wiwiss.ng4j.impl.NamedGraphImpl;
import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;

public class Builder {
	public static final String CATEGORIES_FOLDER = "categories";
	public static final String MODELS_FOLDER = "models";
	public static final String RULESETS_FOLDER = "rulesets";
	public static final String OWL_SPECIALIZED_RULESET = RULESETS_FOLDER
			+ "/owl-specialized.rules";
	private static final String VALUE_CLASS_NAME_FORMAT = "%sValue";
	private static final String ACTUAL_CLASS_NAME_FORMAT = "Actual%s";
	private static final String VALUE_INDIVIDUAL_NAME_FORMAT = "value-%s";
	private static final String DIMENSION_INDIVIDUAL_NAME_FORMAT = "dimension-%s";
	private static final String PARAMETER_INDIVIDUAL_NAME_FORMAT = "parameter-%s";
	private static final String CONTEXT_MODEL_VOCABULARY_NS_PREFIX = "c";
	private static final Map<String, String> LANG_OUTPUT_EXT = new HashMap<String, String>();

	private static final Map<String, String> categorySourceNames = new HashMap<String, String>();

	private BuilderConfiguration config;

	static {
		// setup category source name shortcuts
		categorySourceNames
				.put("paginegialle",
						"it.polimi.dei.dbgroup.pedigree.contextmodel.builder.sources.PagineGialle");
		categorySourceNames
				.put("unspsc",
						"it.polimi.dei.dbgroup.pedigree.contextmodel.builder.sources.UNSPSC");

		// setup language-extension mappings
		LANG_OUTPUT_EXT.put("RDF/XML-ABBREV", "owl");
		LANG_OUTPUT_EXT.put("RDF/XML", "owl");
		LANG_OUTPUT_EXT.put("N3", "n3");
		LANG_OUTPUT_EXT.put("N-TRIPLE", "ntriple");

		// register the MakeSkolem builtin
		BuiltinRegistry.theRegistry.register(new MakeSkolem());
	}

	private static final class EntityCountReport {
		public int categories = 0;
		public int dimensions = 0;
		public int values = 0;
		public int parameters = 0;
	}

	public Builder(BuilderConfiguration config) {
		this.config = config;
	}

	public void run() {
		try {
			// deserialize categories (parsing possible references)
			pln("Deserializing categories...");
			List<Category> categories = deserializeCategories(getDestinationFile(
					config.getCategoriesFileName(), CATEGORIES_FOLDER, null));

			// dump categories
			for (Category cat : categories) {
				p(cat.toString());
			}

			// count entities
			EntityCountReport report = countEntites(categories);
			pln("Categories: " + report.categories);
			pln("Dimensions: " + report.dimensions);
			pln("Values: " + report.values);
			pln("Parameters: " + report.parameters);

			// build model
			pln("Building model...");
			OntModel ontModel = buildModel(categories);

			// read the included models and cache the raw graphs
			Map<String, Model> models = new HashMap<String, Model>();
			Map<String, Graph> graphs = new HashMap<String, Graph>();
			models.put(config.getModelURI(), ontModel.getBaseModel());
			graphs.put(config.getModelURI(), ontModel.getBaseModel().getGraph());
			for (IncludedModelData includedModelData : config
					.getIncludedModels()) {
				pln("Reading included model " + includedModelData.URI);
				Model includedModel = ModelFactory.createDefaultModel();
				includedModel.read(includedModelData.path,
						includedModelData.URI, null);
				models.put(includedModelData.URI, includedModel);
				graphs.put(includedModelData.URI, includedModel.getGraph());
			}

			GenericRuleReasoner reasoner = null;
			if (config.shouldClassify() || config.shouldCreateBackwardRuleset()) {
				reasoner = new GenericRuleReasoner(Rule
						.rulesFromURL(OWL_SPECIALIZED_RULESET));
				reasoner.setTransitiveClosureCaching(false);
				reasoner.setOWLTranslation(false);
			}

			if (config.shouldClassify()) {
				pln("Classifying models...");

				for (String modelURI : models.keySet()) {
					Model rawModel = models.get(modelURI);
					FBRuleInfGraph infGraph = (FBRuleInfGraph) reasoner
							.bind(rawModel.getGraph());
					infGraph.prepare();
					// copy all the triples to a non-inference graph (so that
					// it's
					// faster to serialize)
					Graph graph = Factory.createDefaultGraph();
					for (ExtendedIterator<Triple> it = infGraph.find(
							com.hp.hpl.jena.graph.Node.ANY,
							com.hp.hpl.jena.graph.Node.ANY,
							com.hp.hpl.jena.graph.Node.ANY); it.hasNext();) {
						graph.add(it.next());
					}
					Model classifiedModel = ModelFactory
							.createModelForGraph(graph);
					models.put(modelURI, classifiedModel);
				}
			}

			Model contextModel = models.get(config.getModelURI());

			// save model under models
			File modelFile = getModelFile(config.getOutputFileName(),
					LANG_OUTPUT_EXT.get(config.getOutputLanguage()));
			pln("Saving model as " + config.getOutputLanguage() + " to file "
					+ modelFile.getName());
			RDFWriter writer = contextModel.getWriter(config
					.getOutputLanguage());

			try {
				writer.setProperty("showXMLDeclaration", config
						.isShowXMLDeclaration() ? "true" : "false");
			} catch (UnknownPropertyException upe) {
			}

			try {
				writer.setProperty("showDoctypeDeclaration", config
						.isShowDocTypeDeclaration() ? "true" : "false");
			} catch (UnknownPropertyException upe) {
			}

			try {
				writer.setProperty("xmlbase", config.getModelURI());
			} catch (UnknownPropertyException upe) {
			}
			OutputStream os = new FileOutputStream(modelFile);
			writer.write(contextModel, os, config.getModelURI());
			os.close();

			if (config.isCreateTDBStore()) {
				String storeDir = MODELS_FOLDER + "/"
						+ config.getOutputFileName();
				File storeDirFile = new File(storeDir);
				deleteDirectory(storeDirFile);
				pln("Creating TriG dataset for TDB store");
				NamedGraphSet ngs = new NamedGraphSetImpl();
				for (String modelURI : models.keySet()) {
					Model model = models.get(modelURI);
					NamedGraph namedGraph = new NamedGraphImpl(modelURI, model
							.getGraph());
					ngs.addGraph(namedGraph);
				}
				// Model contextModel = ngs.asJenaModel(config.getModelURI());
				// contextModel.read("file:" + MODELS_FOLDER + "/"
				// + modelFile.getName(), config.getModelURI(), null);
				// for (IncludedModelData includedModelData : config
				// .getIncludedModels()) {
				// Model includedModel = ngs
				// .asJenaModel(includedModelData.URI);
				// includedModel.read(includedModelData.path,
				// includedModelData.URI, null);
				// }
				String dsFileName = MODELS_FOLDER + "/"
						+ config.getOutputFileName() + "_dataset.trig";
				ngs.write(new FileOutputStream(dsFileName), "TRIG", config
						.getModelURI());
				pln("Creating TDB store in folder " + storeDir);
				DatasetGraphTDB ds = TDBFactory.createDatasetGraph(storeDir);
				TDBLoader loader = new TDBLoader();
				loader.setGenerateStats(false);
				loader.setShowProgress(false);
				loader.loadDataset(ds, "file:" + dsFileName);
				TDB.sync(ds);
				pln("Creating TDB stats.opt file");
				Item statsItem = StatsCollector.gatherTDB(ds
						.getGraphTDB(com.hp.hpl.jena.graph.Node
								.createURI("urn:x-arq:UnionGraph")));
				File statsFile = new File(storeDir + "/stats.opt");
				BufferedWriter statsWriter = new BufferedWriter(new FileWriter(
						statsFile));
				statsWriter.write(statsItem.toString());
				statsWriter.close();
				ds.close();
				TDB.closedown();
				// Model tdbModel = TDBFactory.createModel(storeDir);
				// GraphTDB tdbGraph = (GraphTDB) tdbModel.getGraph();
				// p("Bulk loading TDB store");
				// TDBLoader.load(tdbGraph, "file:" + MODELS_FOLDER + "/"
				// + modelFile.getName());
				// TDB.sync(tdbModel);
				// p("Creating TDB stats.opt file");
				// Item statsItem = StatsCollector.gatherTDB(tdbGraph);
				// File statsFile = new File(storeDir + "/stats.opt");
				// BufferedWriter statsWriter = new BufferedWriter(new
				// FileWriter(
				// statsFile));
				// statsWriter.write(statsItem.toString());
				// statsWriter.close();
				// tdbModel.close();
				// TDB.closedown();
				File storeFile = getDestinationFile(config.getOutputFileName()
						+ "_tdb", MODELS_FOLDER, "zip");
				pln("Creating TDB store archive in " + storeFile.getName());
				zipDir(storeDir, storeFile);
				// store deletion doesn't work because tdb leaks some file
				// handle that prevents file from
				// being deleted
				// p("Deleting TDB store folder " + storeDir);
				// recursiveDelete(new File(storeDir));
			}

			if (config.shouldCreateBackwardRuleset()) {
				pln("Creating backward ruleset in "
						+ config.getOutputFileName() + ".rules");

				String rulesetPath = MODELS_FOLDER + "/"
						+ config.getOutputFileName() + ".rules";
				File rulesetFile = new File(rulesetPath);
				if (rulesetFile.exists())
					rulesetFile.delete();

				// setup the MultiUnion graph and init namespace prefix
				// replacements
				MultiUnion unionGraph = new MultiUnion();
				Map<String, String> namespacePrefixMap = new HashMap<String, String>();
				int includedModelIndex = 1;
				for (String graphURI : graphs.keySet()) {
					Graph graph = graphs.get(graphURI);
					unionGraph.addGraph(graph);
					namespacePrefixMap.put(graphURI + "#", "m"
							+ (includedModelIndex++) + ":");
				}
				
				Set<String> tabulatedPropertyURIs = new HashSet<String>();
				// find all inverse properties that must be tabulated
				for(ExtendedIterator<Triple> it = unionGraph.find(com.hp.hpl.jena.graph.Node.ANY, OWL.inverseOf.asNode(), com.hp.hpl.jena.graph.Node.ANY); it.hasNext(); ) {
					Triple t = it.next();
					if(t.getSubject().isURI()) tabulatedPropertyURIs.add(t.getSubject().getURI());
					if(t.getObject().isURI()) tabulatedPropertyURIs.add(t.getObject().getURI());
				}

				// classify the uniongraph
				FBRuleInfGraph classifiedGraph = (FBRuleInfGraph) reasoner
						.bind(unionGraph);
				classifiedGraph.prepare();

				PrintWriter rulesetWriter = new PrintWriter(rulesetFile);
				for (String namespace : namespacePrefixMap.keySet()) {
					rulesetWriter.println("@prefix "
							+ namespacePrefixMap.get(namespace) + " <"
							+ namespace + ">.");
				}
				StringBuilder rulesetSb = new StringBuilder();
				// use tableAll, it's faster
//				for(String tabulatedPropertyURI : tabulatedPropertyURIs) {
//					rulesetSb.append("-> table(");
//					rulesetSb.append(tabulatedPropertyURI);
//					rulesetSb.append(").\n");
//				}
				rulesetSb.append("-> tableAll().\n");
				for (Rule rule : classifiedGraph.getBRules()) {
					rulesetSb.append(rule.toString());
					rulesetSb.append("\n");
				}
				String rulesetString = rulesetSb.toString();
				for (String namespace : namespacePrefixMap.keySet()) {
					rulesetString = rulesetString.replaceAll(namespace,
							namespacePrefixMap.get(namespace));
				}
				rulesetWriter.print(rulesetString);
				rulesetWriter.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.flush();
		} finally {
			pln("Done.");
		}
	}

	// private static final void recursiveDelete(File file) throws Exception {
	// System.gc();
	// if (file.isDirectory()) {
	// for (File child : file.listFiles()) {
	// recursiveDelete(child);
	// }
	// }
	// file.deleteOnExit();
	// if (!file.delete()) {
	// throw new Exception("cannot delete file " + file.getPath());
	// }
	// }

	private static final void zipDir(String dir2zip, File destFile)
			throws IOException {
		ZipOutputStream zos = new ZipOutputStream(
				new FileOutputStream(destFile));
		zipDir("", new File(dir2zip), zos, new byte[2156]);
		zos.close();
	}

	private static final void zipDir(String prefix, File directory,
			ZipOutputStream zos, byte[] buffer) throws IOException {
		for (File child : directory.listFiles()) {
			String childPath = prefix;
			if (childPath.length() > 0)
				childPath += "/";
			childPath += child.getName();
			if (child.isDirectory()) {
				zipDir(childPath, child, zos, buffer);
			} else {
				FileInputStream fis = new FileInputStream(child);
				ZipEntry entry = new ZipEntry(childPath);
				zos.putNextEntry(entry);
				int read;
				while ((read = fis.read(buffer)) != -1) {
					zos.write(buffer, 0, read);
				}
				fis.close();
			}
		}
	}

	private static final String findCategorySourceClassByName(String name) {
		String lcaseName = name.toLowerCase();
		if (categorySourceNames.containsKey(lcaseName))
			return categorySourceNames.get(lcaseName);
		return name;
	}

	private static File getCategoriesFile(String name) {
		return getDestinationFile(name, CATEGORIES_FOLDER, "xml");
	}

	private static File getModelFile(String name, String ext) {
		return getDestinationFile(name, MODELS_FOLDER, ext);
	}

	private static File getDestinationFile(String name, String folderName,
			String ext) {
		File folder = new File(folderName);
		if (!folder.exists()) {
			pln("Creating " + folderName + " folder");
			folder.mkdir();
		}
		String fileName = name;
		if (ext != null)
			fileName += "." + ext;
		return new File(folder, fileName);
	}

	private static void serializeCategories(File categoriesFile,
			List<Category> categories) throws Exception {
		pln("Serializing categories to " + categoriesFile.getAbsolutePath());
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();

		Document document = builder.newDocument();
		Element root = document.createElement("categories");
		document.appendChild(root);
		serializeCategories(document, root, categories);
		FileWriter fw = new FileWriter(categoriesFile);
		XMLSerializer ser = new XMLSerializer(fw, new OutputFormat(document,
				"utf-8", true));
		ser.serialize(document);
		fw.flush();
		fw.close();
	}

	private static void serializeCategories(Document doc, Element parent,
			List<Category> categories) {
		for (Category category : categories) {
			Element el = doc.createElement("category");
			parent.appendChild(el);
			serializeNode(doc, category, el);
			for (String clazz : category.getClasses()) {
				el.appendChild(createTextElement(doc, "class", clazz));
			}
			for (Parameter parameter : category.getParameters()) {
				Element parEl = doc.createElement("parameter");
				serializeNode(doc, parameter, parEl);
				parEl.setAttribute("type", parameter.getType().getURI());
				el.appendChild(parEl);
			}
			serializeCategories(doc, el, category.getSubCategories());
		}
	}

	private static void serializeNode(Document doc, Node node, Element el) {
		serializeLocalizedAttribute(node.getName(), "name", doc, el);
		serializeLocalizedAttribute(node.getDescription(), "description", doc,
				el);
		if (!StringUtils.isEmpty(node.getId()))
			el.setAttribute("id", node.getId());
	}

	private static void serializeLocalizedAttribute(
			LocalizedAttribute attribute, String name, Document doc, Element el) {
		if (attribute.getDefaultValue() != null)
			el.appendChild(createTextElement(doc, name, attribute
					.getDefaultValue()));
		for (String lang : attribute.getLocalizedValues().keySet()) {
			String text = attribute.get(lang);
			el.appendChild(createTextElement(doc, name, text, lang));
		}
	}

	private static Element createTextElement(Document doc, String tag,
			String text) {
		return createTextElement(doc, tag, text, null);
	}

	private static Element createTextElement(Document doc, String tag,
			String text, String lang) {
		Element el = doc.createElement(tag);
		el.appendChild(doc.createTextNode(text));
		if (lang != null)
			el.setAttribute("xml:lang", lang);
		return el;
	}

	private static class CategoriesHandler extends DefaultHandler {
		private enum Type {
			Categories, Category, Parameter, Name, Description, Reference, Class
		};

		private static final Map<String, Type> typeMappings = new HashMap<String, Type>();
		static {
			typeMappings.put("categories", Type.Categories);
			typeMappings.put("category", Type.Category);
			typeMappings.put("parameter", Type.Parameter);
			typeMappings.put("name", Type.Name);
			typeMappings.put("description", Type.Description);
			typeMappings.put("reference", Type.Reference);
			typeMappings.put("class", Type.Class);
		}

		private static final Map<Type, Type[]> allowedTypes = new HashMap<Type, Type[]>();
		static {
			Type[] noTypes = new Type[0];
			allowedTypes.put(Type.Categories, new Type[] { Type.Category });
			allowedTypes.put(Type.Category, new Type[] { Type.Category,
					Type.Parameter, Type.Name, Type.Description,
					Type.Reference, Type.Class });
			allowedTypes.put(Type.Parameter, new Type[] { Type.Name,
					Type.Description, Type.Class });
			allowedTypes.put(Type.Name, noTypes);
			allowedTypes.put(Type.Description, noTypes);
			allowedTypes.put(Type.Reference, noTypes);
			allowedTypes.put(Type.Class, noTypes);
		}
		private static final Type[] allowedRootTypes = new Type[] { Type.Categories };

		private final Stack<Type> typeStack = new Stack<Type>();
		private final Stack<Category> categoryStack = new Stack<Category>();
		private final Set<String> foundIds = new HashSet<String>();
		private Category parent = null;
		private Node node = null;
		private Type type = null;
		private String lang = null;
		private StringBuilder sb = null;
		public List<Category> rootCategories = new ArrayList<Category>();
		private Locator locator = null;

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if (type == Type.Category) {
				parent = categoryStack.isEmpty() ? null : categoryStack.pop();
			} else if (type == Type.Name || type == Type.Description) {
				LocalizedAttribute la = (type == Type.Name) ? node.getName()
						: node.getDescription();
				if (lang == null)
					la.setDefaultValue(sb.toString());
				else
					la.set(lang, sb.toString());
				sb = null;
			} else if (type == Type.Class) {
				node.getClasses().add(sb.toString());
				sb = null;
			}
			type = typeStack.isEmpty() ? null : typeStack.pop();
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes atts) throws SAXException {
			Type oldType = type;

			type = typeMappings.get(qName);
			if (type == null)
				throw new SAXParseException("unrecognized element " + qName
						+ " inside " + oldType + " element", locator);

			Type[] allowed;
			if (oldType == null)
				allowed = allowedRootTypes;
			else
				allowed = allowedTypes.get(oldType);

			if (Arrays.binarySearch(allowed, type) < 0)
				throw new SAXParseException(type
						+ " elements are not allowed inside " + oldType
						+ " elements", locator);

			typeStack.push(oldType);

			if (type == Type.Category) {
				Category c = new Category();
				if (parent != null) {
					parent.getSubCategories().add(c);
					categoryStack.push(parent);
				} else {
					rootCategories.add(c);
				}
				c.setParent(parent);

				parent = c;
				node = c;
			} else if (type == Type.Parameter) {
				Parameter p = new Parameter();
				String dataType = atts.getValue("type");
				if (dataType != null) {
					if (!dataType.contains(":"))
						dataType = XSDDatatype.XSD + "#" + dataType;
					RDFDatatype rdfType = TypeMapper.getInstance()
							.getTypeByName(dataType);
					if (rdfType == null)
						pln("WARNING: unknown parameter type " + dataType
								+ " at line " + locator.getLineNumber()
								+ ", col " + locator.getColumnNumber());
					else
						p.setType(rdfType);
				}
				parent.getParameters().add(p);
				p.setCategory(parent);

				node = p;
			} else if (type == Type.Reference) {
				String sourceClassName = atts.getValue("source");
				if (StringUtils.isEmpty(sourceClassName))
					throw new SAXParseException(
							"Missing or empty source attribute in reference element",
							locator);
				int limit = -1;
				String limitStr = atts.getValue("limit");
				if (!StringUtils.isEmpty(limitStr)) {
					try {
						limit = Integer.parseInt(limitStr);
					} catch (NumberFormatException nfex) {
						throw new SAXParseException(
								"reference with non integer limit " + limitStr,
								locator, nfex);
					}
					if (limit < 0)
						throw new SAXParseException(
								"reference with negative limit " + limit,
								locator);
				}

				List<Category> referenceCategories;
				try {
					referenceCategories = parseReference(sourceClassName, limit);
				} catch (Exception ex) {
					throw new SAXParseException("Cannot parse reference",
							locator, ex);
				}

				if (parent != null) {
					parent.getSubCategories().addAll(referenceCategories);
				} else {
					rootCategories.addAll(referenceCategories);
				}
			} else if (type == Type.Name || type == Type.Description) {
				String l = atts.getValue("xml:lang");
				if (!StringUtils.isEmpty(l))
					lang = l;
				else
					lang = null;
				sb = new StringBuilder();
			} else if (type == Type.Class) {
				sb = new StringBuilder();
			}

			if (type == Type.Category || type == Type.Parameter) {
				String id = atts.getValue("id");
				if (!StringUtils.isEmpty(id)) {
					// check that the ID is unique
					if (foundIds.contains(id))
						throw new SAXParseException(
								"Found duplicate ID: " + id, locator);
					node.setId(id);
					foundIds.add(id);
				} else
					node.setId(null);
			}
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (type == Type.Name || type == Type.Description
					|| type == Type.Class)
				sb.append(ch, start, length);
		}

		@Override
		public void setDocumentLocator(Locator locator) {
			this.locator = locator;
		}

	}

	private static List<Category> parseReference(String sourceClassName,
			int limit) throws Exception {
		if (sourceClassName == null)
			throw new Exception("category source class cannot be null");
		sourceClassName = findCategorySourceClassByName(sourceClassName);
		Class<?> sourceClass = null;
		try {
			sourceClass = Class.forName(sourceClassName);
		} catch (Exception ex2) {
			throw new Exception("cannot find category source class "
					+ sourceClassName, ex2);
		}

		if (!ICategorySource.class.isAssignableFrom(sourceClass))
			throw new Exception("category source class " + sourceClassName
					+ " does not implement ICategorySource interface");

		ICategorySource categorySource = null;

		try {
			categorySource = (ICategorySource) sourceClass.newInstance();
		} catch (Exception ex2) {
			throw new Exception(
					"cannot instantiate a category source of class "
							+ sourceClassName, ex2);
		}

		String name = categorySource.getName();
		pln("Parsing reference " + name);
		List<Category> categories = null;
		// check if the taxonomy has already been built
		File categoriesFile = getCategoriesFile(name);
		if (!categoriesFile.exists()) {
			pln("Building categories " + name);
			categories = categorySource.getCategories();
			serializeCategories(categoriesFile, categories);
		}
		// we re-read the categories from the serialized file in order to
		// validate them
		pln("Reading categories from file " + categoriesFile.getName());
		categories = deserializeCategories(categoriesFile);

		if (limit > 0)
			pruneCategories(categories, limit);

		return categories;
	}

	private static void pruneCategories(List<Category> categories, int limit) {
		if (limit == 0)
			categories.clear();
		else {
			for (Category category : categories) {
				pruneCategories(category.getSubCategories(), limit - 1);
			}
		}
	}

	private static List<Category> deserializeCategories(File categoriesFile)
			throws Exception {
		if (!categoriesFile.exists())
			throw new ParseException("categories file "
					+ categoriesFile.getName() + " does not exist");
		SAXParser parser = new SAXParser();
		CategoriesHandler handler = new CategoriesHandler();
		parser.setContentHandler(handler);
		parser.parse(new InputSource(new FileInputStream(categoriesFile)));
		return handler.rootCategories;
	}

	private OntModel buildModel(List<Category> categories) throws Exception {
		// create model with no reasoning
		// OntDocumentManager docManager = OntDocumentManager.getInstance();
		// docManager.addAltEntry(ContextModelVocabulary.URI,
		// ContextModelFactory.CONTEXT_MODEL_VOCABULARY_ALT_URI);

		OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);

		// ensure that the context model vocabulary is imported
		// docManager.loadImport(m, ContextModelVocabulary.URI);

		// // import each included model
		// for (IncludedModelData includedModel : config.getIncludedModels()) {
		// if (!ContextModelVocabulary.URI.equals(includedModel.URI)) {
		// docManager.addAltEntry(includedModel.URI, includedModel.path);
		// docManager.loadImport(m, includedModel.URI);
		// }
		// }

		// set namespace prefix
		String ns = config.getModelURI() + "#";
		m.setNsPrefix("", ns);
		m.setNsPrefix(CONTEXT_MODEL_VOCABULARY_NS_PREFIX,
				ContextModelVocabulary.NS);

		// create ontology and import context model
		Ontology ontology = m.createOntology(config.getModelURI());
		ontology.addVersionInfo(config.getVersion());
		ontology.addImport(ContextModelVocabulary.Ontology);

		if (config.getUseMetaModel()) {
			// import context meta model
			ontology.addImport(ContextMetaModel.Ontology);
		}

		// if(true) return m;

		Individual specificationIndividual = null;
		if (config.getUseMetaModel()) {
			// create context specification
			specificationIndividual = m.createIndividual(config
					.getSpecificationURI(),
					ContextMetaModel.ContextSpecification);
			specificationIndividual.addVersionInfo(config.getVersion());
		}

		// prepare initial context
		Context initialContext = new Context(categories, null, null, "");

		// init data structures
		// Set<OntClass> definedAssignmentClasses = new HashSet<OntClass>();
		// Set<OntClass> definedValueClasses = new HashSet<OntClass>();
		Deque<Context> queue = new LinkedList<Context>();

		// start parsing categories
		queue.add(initialContext);
		while (!queue.isEmpty()) {
			Context context = queue.poll();

			// definedAssignmentClasses.clear();

			// parse dimension categories
			int dimensionIndex = 1;
			for (Category dimensionCategory : context.getDimensionCategories()) {
				// create dimension identifier
				String dimensionIdentifier = dimensionCategory.getId();
				String dimensionName = dimensionIdentifier;
				if (dimensionIdentifier == null) {
					dimensionIdentifier = createIdentifier(context
							.getIdentifier(), dimensionIndex++);
					dimensionName = String.format(
							DIMENSION_INDIVIDUAL_NAME_FORMAT,
							dimensionIdentifier);
				}

				// create formal dimension individual
				Individual dimensionIndividual = createIndividual(m, ns,
						dimensionName, dimensionCategory,
						ContextModelVocabulary.Dimension);

				if (config.getUseMetaModel()) {
					// set rootDimension or valueSubDimension properties
					if (context.getValueIndividual() == null) {
						specificationIndividual.addProperty(
								ContextMetaModel.rootDimension,
								dimensionIndividual);
					} else {
						context.getValueIndividual().addProperty(
								ContextMetaModel.valueSubDimension,
								dimensionIndividual);
					}
				}

				// parse dimension parameters
				// for (int parameterIndex = 1; parameterIndex <=
				// dimensionCategory
				// .getParameters().size(); parameterIndex++) {
				// // create parameter identifier
				// String parameterIdentifier = createIdentifier(
				// dimensionIdentifier, parameterIndex);
				//
				// Parameter parameter = dimensionCategory.getParameters()
				// .get(parameterIndex - 1);

				// create parameter stuff
				// Individual parameterIndividual = createParameter(parameter,
				// parameterIdentifier, m, m
				// .getOntClass(ContextModel.FormalParameter
				// .getURI()));

				// assign label and comment to parameter
				// assignLabelAndComment(parameterIndividual, parameter);
				//
				// if (config.getUseMetaModel()) {
				// // set dimensionParameter property
				// dimensionIndividual.addProperty(
				// ContextMetaModel.dimensionParameter,
				// parameterIndividual);
				// }
				// }

				// get a camelized identifier
				String camelizedDimensionName = camelize(dimensionName);

				// create values class
				String valueClassName = String.format(VALUE_CLASS_NAME_FORMAT,
						camelizedDimensionName);
				OntClass valueClass = m.createClass(ns + valueClassName);
				valueClass.addSuperClass(ContextModelVocabulary.Value);

				// create actual dimension class
				String actualDimensionClassName = String.format(
						ACTUAL_CLASS_NAME_FORMAT, camelizedDimensionName);
				OntClass actualDimensionClass = m.createClass(ns
						+ actualDimensionClassName);

				// add superclass
				actualDimensionClass
						.addSuperClass(ContextModelVocabulary.ActualDimension);

				// add dimension value restriction
				String dimensionValueRestrictionURI = ns + actualDimensionClassName
						+ "-DimensionValueRestriction";
				actualDimensionClass.addSuperClass(m
						.createSomeValuesFromRestriction(
								dimensionValueRestrictionURI,
								ContextModelVocabulary.dimensionValue,
								valueClass));

				// add formal dimension restriction
				String formalDimensionRestrictionURI = ns + actualDimensionClassName
						+ "-FormalDimensionRestriction";
				actualDimensionClass.addSuperClass(m.createHasValueRestriction(
						formalDimensionRestrictionURI,
						ContextModelVocabulary.formalDimension,
						dimensionIndividual));

				// add parent dimension restriction
				// TODO: reimplement model formality level
				if (context.getActualDimensionClass() != null
						&& context.getValueIndividual() != null) {
					String parentDimensionValueRestrictionURI = ns + actualDimensionClassName
							+ "-ParentDimensionValueRestriction";
					RDFList parentDimensionIntersectionList = m.createList();
					parentDimensionIntersectionList = parentDimensionIntersectionList
							.with(context.getActualDimensionClass());
					parentDimensionIntersectionList = parentDimensionIntersectionList
							.with(m.createHasValueRestriction(
									parentDimensionValueRestrictionURI,
									ContextModelVocabulary.dimensionValue,
									context.getValueIndividual()));
					String inContextRestrictionURI = ns + actualDimensionClassName
							+ "-InContextRestriction";
					String hasDimensionRestrictionURI = ns + actualDimensionClassName
							+ "-HasDimensionRestriction";
					String hasDimensionIntersectionURI = ns + actualDimensionClassName
							+ "-HasDimensionIntersection";
					actualDimensionClass
							.addSuperClass(m
									.createSomeValuesFromRestriction(
											inContextRestrictionURI,
											ContextModelVocabulary.inContext,
											m
													.createSomeValuesFromRestriction(
															hasDimensionRestrictionURI,
															ContextModelVocabulary.hasDimension,
															m
																	.createIntersectionClass(
																			hasDimensionIntersectionURI,
																			parentDimensionIntersectionList))));
				}

				// if (config.getComplexity() == ModelComplexityLevel.HIGH) {
				// // set equivalent restrictions
				// assignmentClass
				// .addEquivalentClass(m
				// .createHasValueRestriction(
				// null,
				// m
				// .getObjectProperty(ContextModel.assignmentDimension
				// .getURI()),
				// dimensionIndividual));
				// assignmentClass
				// .addEquivalentClass(m
				// .createSomeValuesFromRestriction(
				// null,
				// m
				// .getObjectProperty(ContextModel.dimensionAssignmentValue
				// .getURI()),
				// valueClass));
				//
				// // set superclass
				// RDFList intersectionList = m.createList();
				// intersectionList = intersectionList.with(context
				// .getActualDimensionClass());
				// intersectionList = intersectionList.with(m
				// .createCardinalityRestriction(null,
				// assignmentProperty, 1));
				// if (context.getDimensionAssignmentProperty() != null)
				// intersectionList = intersectionList.with(m
				// .createHasValueRestriction(null, context
				// .getDimensionAssignmentProperty(),
				// context.getValueIndividual()));
				// assignmentClass.addSuperClass(m.createIntersectionClass(
				// null, intersectionList));
				// } else if (config.getComplexity() ==
				// ModelComplexityLevel.LOW) {
				// // set equivalent restriction
				// RDFList equivalentIntersectionList = m.createList();
				// equivalentIntersectionList = equivalentIntersectionList
				// .with(m.createCardinalityRestriction(null,
				// assignmentProperty, 1));
				// equivalentIntersectionList = equivalentIntersectionList
				// .with(m
				// .createHasValueRestriction(
				// null,
				// m
				// .getObjectProperty(ContextModel.assignmentDimension
				// .getURI()),
				// dimensionIndividual));
				// assignmentClass.addEquivalentClass(m
				// .createIntersectionClass(null,
				// equivalentIntersectionList));
				//
				// // set superclass
				// Resource superclass;
				// if (context.getDimensionAssignmentProperty() != null) {
				// RDFList superclassIntersectionList = m.createList();
				// superclassIntersectionList = superclassIntersectionList
				// .with(context.getActualDimensionClass());
				// superclassIntersectionList = superclassIntersectionList
				// .with(m.createHasValueRestriction(null, context
				// .getDimensionAssignmentProperty(),
				// context.getValueIndividual()));
				// superclass = m.createIntersectionClass(null,
				// superclassIntersectionList);
				// } else {
				// superclass = context.getActualDimensionClass();
				// }
				// assignmentClass.addSuperClass(superclass);
				// }

				// parse value categories
				int valueIndex = 1;
				for (Category valueCategory : dimensionCategory
						.getSubCategories()) {
					// create value identifier and name
					String valueIdentifier = valueCategory.getId();
					String valueName = valueIdentifier;
					if (valueIdentifier == null) {
						valueIdentifier = createIdentifier(dimensionIdentifier,
								valueIndex++);
						valueName = String.format(VALUE_INDIVIDUAL_NAME_FORMAT,
								valueIdentifier);
					}

					// create value individual
					Individual valueIndividual = createIndividual(m, ns,
							valueName, valueCategory, valueClass,
							ContextModelVocabulary.Value);

					if (config.getUseMetaModel()) {
						// set dimensionValue property
						dimensionIndividual.addProperty(
								ContextMetaModel.dimensionValue,
								valueIndividual);
					}

					// parse value parameters
					int valueParameterIndex = 1;
					for (Parameter valueParameter : valueCategory
							.getParameters()) {
						// create parameter identifier and name
						String parameterIdentifier = valueParameter.getId();
						String parameterName = parameterIdentifier;
						if (parameterIdentifier == null) {
							parameterIdentifier = createIdentifier(
									valueIdentifier, valueParameterIndex++);
							parameterName = String.format(
									PARAMETER_INDIVIDUAL_NAME_FORMAT,
									parameterIdentifier);
						}

						// create parameter individual
						Individual parameterIndividual = createIndividual(m,
								ns, parameterName, valueParameter,
								ContextModelVocabulary.Parameter);

						// create camelized parameter name
						String camelizedParameterName = camelize(parameterName);

						// create actual parameter class
						String actualParameterClassName = String.format(
								ACTUAL_CLASS_NAME_FORMAT,
								camelizedParameterName);
						OntClass actualParameterClass = m.createClass(ns
								+ actualParameterClassName);

						// add super class
						actualParameterClass
								.addSuperClass(ContextModelVocabulary.ActualParameter);

						// add formal parameter restriction
						String formalParameterRestrictionURI = ns + actualParameterClassName
								+ "-FormalParameterRestriction";
						actualParameterClass.addSuperClass(m
								.createHasValueRestriction(
										formalParameterRestrictionURI,
										ContextModelVocabulary.formalParameter,
										parameterIndividual));

						// add parameter value restriction
						if (valueParameter.getType() != null) {
							String parameterValueRestrictionURI = ns + actualParameterClassName
									+ "-ParameterValueRestriction";
							actualParameterClass
									.addSuperClass(m
											.createSomeValuesFromRestriction(
													parameterValueRestrictionURI,
													ContextModelVocabulary.parameterValue,
													m
															.createResource(valueParameter
																	.getType()
																	.getURI())));
						}

						// add parent value restriction
						String parameterOfValueRestrictionURI = ns + actualParameterClassName
								+ "-ParameterOfValueRestriction";
						actualParameterClass
								.addSuperClass(m
										.createHasValueRestriction(
												parameterOfValueRestrictionURI,
												ContextModelVocabulary.parameterOfValue,
												valueIndividual));

						if (config.getUseMetaModel()) {
							// set valueParameter property
							valueIndividual.addProperty(
									ContextMetaModel.valueParameter,
									parameterIndividual);
						}
					}

					// if value has subdimension, enqueue a new context
					if (valueCategory.getSubCategories().size() > 0) {
						Context newContext = new Context(valueCategory
								.getSubCategories(), actualDimensionClass,
								valueIndividual, valueIdentifier);
						queue.add(newContext);
					}
				}
			}

			// removed disjoint closure. Jena doesn't support OWL2, so we can't
			// use the AllDisjointClasses class.
			// In order to state that a group of classes is mutually disjoint,
			// we should add a disjointWith property
			// between any couple of classes in the group, and this would
			// clutter the ontology.
			// // set as disjoint all child assignment classes
			// OntClass firstAssignmentClass = null;
			// Iterator<OntClass> it = definedAssignmentClasses.iterator();
			// while (it.hasNext()) {
			// if (firstAssignmentClass == null)
			// firstAssignmentClass = it.next();
			// else
			// firstAssignmentClass.addDisjointWith(it.next());
			// }
		}

		// removed disjoint closure, see above
		// // set as disjoint all value classes
		// OntClass firstValueClass = null;
		// Iterator<OntClass> it = definedValueClasses.iterator();
		// while (it.hasNext()) {
		// if (firstValueClass == null)
		// firstValueClass = it.next();
		// else
		// firstValueClass.addDisjointWith(it.next());
		// }

		return m;
	}

	private static Individual createIndividual(OntModel m, String ns,
			String name, Node n, Resource parentClass) {
		return createIndividual(m, ns, name, n, parentClass, parentClass);
	}

	private static Individual createIndividual(OntModel m, String ns,
			String name, Node n, Resource parentClass,
			Resource customClassesParentClass) {
		Individual individual = m.createIndividual(ns + name, parentClass);
		assignLabelAndComment(individual, n);
		addToClasses(n, ns, individual, customClassesParentClass, m);
		return individual;
	}

	private static void assignLabelAndComment(OntResource i, Node n) {
		if (n.getName().getDefaultValue() != null)
			i.addLabel(n.getName().getDefaultValue(), null);
		for (String lang : n.getName().getLocalizedValues().keySet()) {
			String name = n.getName().get(lang);
			i.addLabel(name, lang);
		}
		if (n.getDescription().getDefaultValue() != null)
			i.addComment(n.getDescription().getDefaultValue(), null);
		for (String lang : n.getDescription().getLocalizedValues().keySet()) {
			String desc = n.getDescription().get(lang);
			i.addComment(desc, lang);
		}
	}

	private static void addToClasses(Node n, String ns, Individual i,
			Resource parentClass, OntModel m) {
		for (String className : n.getClasses()) {
			String classURI = ns + className;
			OntClass ontClass = m.createClass(classURI);
			if (!ontClass.hasSuperClass(parentClass))
				ontClass.addSuperClass(parentClass);
			i.addOntClass(ontClass);
		}
	}

	private static String createIdentifier(String parentIdentifier, int index) {
		StringBuilder sb = new StringBuilder();
		if (!StringUtils.isEmpty(parentIdentifier)) {
			sb.append(parentIdentifier);
			sb.append("-");
		}
		sb.append(index);
		return sb.toString();
	}

	private static String camelize(String s) {
		return WordUtils.capitalizeFully(s, new char[] { '-', '_', ' ' });
	}

	private static void pln(String s) {
		System.out.println(s);
		System.out.flush();
	}

	private static void p(String s) {
		System.out.print(s);
		System.out.flush();
	}

	private static boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	private static EntityCountReport countEntites(List<Category> categories) {
		EntityCountReport report = new EntityCountReport();
		countEntitiesRec(categories, report, true);
		return report;
	}

	private static void countEntitiesRec(List<Category> categories,
			EntityCountReport report, boolean areDimensions) {
		report.categories += categories.size();
		if (areDimensions)
			report.dimensions += categories.size();
		else
			report.values += categories.size();
		for (Category category : categories) {
			report.parameters += category.getParameters().size();
			countEntitiesRec(category.getSubCategories(), report,
					!areDimensions);
		}
	}
}
