package it.polimi.dei.dbgroup.pedigree.contextmodel.builder;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.BuilderConfiguration.IncludedModelData;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextModelFactory;
import it.polimi.dei.dbgroup.pedigree.contextmodel.vocabulary.ContextMetaModel;
import it.polimi.dei.dbgroup.pedigree.contextmodel.vocabulary.ContextModel;
import it.polimi.dei.dbgroup.pedigree.pervads.model.vocabulary.PervADsContextModel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.UnknownPropertyException;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.TDBLoader;
import com.hp.hpl.jena.tdb.solver.stats.StatsCollector;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB;

import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;

public class Builder {
	public static final String CATEGORIES_FOLDER = "categories";
	public static final String MODELS_FOLDER = "models";

	private static final String ASSIGNMENT_CLASS_NAME_FORMAT = "Dimension_%s_Assignment";
	private static final String VALUE_CLASS_NAME_FORMAT = "Dimension_%s_Value";
	private static final String ASSIGNMENT_PROPERTY_NAME_FORMAT = "dimension_%s_AssignmentValue";
	private static final String VALUE_INDIVIDUAL_NAME_FORMAT = "value_%s";
	private static final String DIMENSION_INDIVIDUAL_NAME_FORMAT = "dimension_%s";
	private static final String PARAMETER_INDIVIDUAL_NAME_FORMAT = "parameter_%s";
	private static final String CONTEXT_MODEL_NS_PREFIX = "c";
	private static final Map<String, String> LANG_OUTPUT_EXT = new HashMap<String, String>();

	private static final Map<String, String> categorySourceNames = new HashMap<String, String>();

	private BuilderConfiguration config;

	static {
		categorySourceNames
				.put("paginegialle",
						"it.polimi.dei.dbgroup.pedigree.contextmodel.builder.sources.PagineGialle");
		categorySourceNames
				.put("unspsc",
						"it.polimi.dei.dbgroup.pedigree.contextmodel.builder.sources.UNSPSC");

		LANG_OUTPUT_EXT.put("RDF/XML-ABBREV", "owl");
		LANG_OUTPUT_EXT.put("RDF/XML", "owl");
		LANG_OUTPUT_EXT.put("N3", "n3");
		LANG_OUTPUT_EXT.put("N-TRIPLE", "ntriple");
	}

	public Builder(BuilderConfiguration config) {
		this.config = config;
	}

	public void run() {
		try {
			// deserialize categories (parsing possible references)
			p("Deserializing categories...");
			List<Category> categories = deserializeCategories(getDestinationFile(
					config.getCategoriesFileName(), CATEGORIES_FOLDER, null));

			// build model
			p("Building model...");
			OntModel model = buildModel(categories);

			// save model under models
			File modelFile = getModelFile(config.getOutputFileName(),
					LANG_OUTPUT_EXT.get(config.getOutputLanguage()));
			p("Saving model as " + config.getOutputLanguage() + " to file "
					+ modelFile.getName());
			RDFWriter writer = model.getWriter(config.getOutputLanguage());

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
			writer.write(model, os, config.getModelURI());
			os.close();

			if (config.isCreateTDBStore()) {
				String storeDir = MODELS_FOLDER + "/"
						+ config.getOutputFileName();
				p("Creating TriG dataset for TDB store");
				NamedGraphSet ngs = new NamedGraphSetImpl();
				Model contextModel = ngs.asJenaModel(config.getModelURI());
				contextModel.read("file:" + MODELS_FOLDER + "/"
						+ modelFile.getName(), config.getModelURI(), null);
				for (IncludedModelData includedModelData : config
						.getTDBStoreIncludedModels()) {
					Model includedModel = ngs
							.asJenaModel(includedModelData.URI);
					includedModel.read(includedModelData.path,
							includedModelData.URI, null);
				}
				String dsFileName = MODELS_FOLDER + "/"
						+ config.getOutputFileName() + "_dataset.trig";
				ngs.write(new FileOutputStream(dsFileName), "TRIG", config
						.getModelURI());
				p("Creating TDB store in folder " + storeDir);
				DatasetGraphTDB ds = TDBFactory.createDatasetGraph(storeDir);
				TDBLoader loader = new TDBLoader();
				loader.setGenerateStats(false);
				loader.setShowProgress(false);
				loader.loadDataset(ds, "file:" + dsFileName);
				TDB.sync(ds);
				p("Creating TDB stats.opt file");
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
				p("Creating TDB store archive in " + storeFile.getName());
				zipDir(storeDir, storeFile);
				// store deletion doesn't work because tdb leaks some file
				// handle that prevents file from
				// being deleted
				// p("Deleting TDB store folder " + storeDir);
				// recursiveDelete(new File(storeDir));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.flush();
		} finally {
			p("Done.");
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
			p("Creating " + folderName + " folder");
			folder.mkdir();
		}
		String fileName = name;
		if (ext != null)
			fileName += "." + ext;
		return new File(folder, fileName);
	}

	private static void serializeCategories(File categoriesFile,
			List<Category> categories) throws Exception {
		p("Serializing categories to " + categoriesFile.getAbsolutePath());
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
			serializeNode(category, el);
			for (Parameter parameter : category.getParameters()) {
				Element parEl = doc.createElement("parameter");
				serializeNode(parameter, parEl);
				parEl.setAttribute("type", parameter.getType());
				el.appendChild(parEl);
			}
			serializeCategories(doc, el, category.getSubCategories());
		}
	}

	private static void serializeNode(Node node, Element el) {
		el.setAttribute("name", node.getName());
		if (!StringUtils.isEmpty(node.getDescription()))
			el.setAttribute("description", node.getDescription());
		if (!StringUtils.isEmpty(node.getId()))
			el.setAttribute("id", node.getId());
		if (!StringUtils.isEmpty(node.getLang()))
			el.setAttribute("xml:lang", node.getLang());
	}

	private static class CategoriesHandler extends DefaultHandler {
		private final Stack<Category> stack = new Stack<Category>();
		private Category parent = null;
		public List<Category> rootCategories = new ArrayList<Category>();
		boolean inEmptyElement = false;
		private Locator locator = null;

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if (inEmptyElement)
				inEmptyElement = false;
			else if (qName.equals("category")) {
				parent = stack.isEmpty() ? null : stack.pop();
			}
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes atts) throws SAXException {
			if (inEmptyElement)
				throw new SAXParseException("No child elements allowed here",
						locator);
			boolean isCategory = localName.equals("category");
			boolean isParameter = !isCategory && localName.equals("parameter");
			boolean isReference = !isCategory && !isParameter
					&& localName.equals("reference");
			if (isCategory || (isParameter && parent != null)) {
				Node n;
				if (isCategory) {
					Category c = new Category();
					if (parent != null) {
						parent.getSubCategories().add(c);
						stack.push(parent);
					} else {
						rootCategories.add(c);
					}
					parent = c;
					n = c;
				} else {
					Parameter p = new Parameter();
					p.setType(atts.getValue("type"));
					parent.getParameters().add(p);
					n = p;
				}

				String name = atts.getValue("name");
				if (StringUtils.isEmpty(name))
					throw new SAXParseException(
							"Missing or empty name attribute in category or parameter element",
							locator);
				n.setName(name);
				String id = atts.getValue("id");
				String desc = atts.getValue("description");
				String lang = atts.getValue("xml:lang");
				if (!StringUtils.isEmpty(id))
					n.setId(id);
				if (!StringUtils.isEmpty(desc))
					n.setDescription(desc);
				if (!StringUtils.isEmpty(lang))
					n.setLang(lang);
			} else if (isReference) {
				String sourceClassName = atts.getValue("source");
				if (StringUtils.isEmpty(sourceClassName))
					throw new SAXParseException(
							"Missing or empty source attribute in reference element",
							locator);
				List<Category> referenceCategories;
				try {
					referenceCategories = parseReference(sourceClassName);
				} catch (Exception ex) {
					throw new SAXParseException("Cannot parse reference",
							locator, ex);
				}

				if (parent != null) {
					parent.getSubCategories().addAll(referenceCategories);
				} else {
					rootCategories.addAll(referenceCategories);
				}
			}

			if (isParameter || isReference)
				inEmptyElement = true;
		}

		@Override
		public void setDocumentLocator(Locator locator) {
			this.locator = locator;
		}

	}

	private static List<Category> parseReference(String sourceClassName)
			throws Exception {
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
		p("Parsing reference " + name);
		List<Category> categories = null;
		// check if the taxonomy has already been built
		File categoriesFile = getCategoriesFile(name);
		if (!categoriesFile.exists()) {
			p("Building categories " + name);
			categories = categorySource.getCategories();
			serializeCategories(categoriesFile, categories);
		} else {
			p("Reading categories from file " + categoriesFile.getName());
			categories = deserializeCategories(categoriesFile);
		}

		return categories;
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
		OntDocumentManager docManager = OntDocumentManager.getInstance();
		docManager.addAltEntry(ContextModel.URI,
				ContextModelFactory.CONTEXT_MODEL_ALT_URI);

		OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		docManager.loadImport(m, ContextModel.URI);

		// set namespace prefix
		String ns = config.getModelURI() + "#";
		m.setNsPrefix("", ns);
		m.setNsPrefix(CONTEXT_MODEL_NS_PREFIX, ContextModel.NS);

		// create ontology and import context model
		Ontology ontology = m.createOntology(config.getModelURI());
		ontology.addVersionInfo(config.getVersion());
		ontology.addImport(ContextModel.Ontology);

		if (config.getUseMetaModel()) {
			// import context meta model
			ontology.addImport(ContextMetaModel.Ontology);
		}

		Individual specificationIndividual = null;
		if (config.getUseMetaModel()) {
			// create context specification
			specificationIndividual = m.createIndividual(config
					.getSpecificationURI(),
					ContextMetaModel.ContextSpecification);
			specificationIndividual.addVersionInfo(config.getVersion());
		}

		// prepare initial context
		Context initialContext = new Context(categories, m
				.getOntClass(ContextModel.DimensionAssignment.getURI()), null,
				null, "");

		// init data structures and start parsing categories
		// Set<OntClass> definedAssignmentClasses = new HashSet<OntClass>();
		// Set<OntClass> definedValueClasses = new HashSet<OntClass>();
		Deque<Context> queue = new LinkedList<Context>();
		queue.add(initialContext);
		while (!queue.isEmpty()) {
			Context context = queue.poll();

			// definedAssignmentClasses.clear();

			// parse dimension categories
			for (int dimensionIndex = 1; dimensionIndex <= context
					.getDimensionCategories().size(); dimensionIndex++) {
				Category dimensionCategory = context.getDimensionCategories()
						.get(dimensionIndex - 1);

				// create dimension identifier
				String dimensionIdentifier = createIdentifier(context
						.getIdentifier(), dimensionIndex);

				// create formal dimension individual
				Individual dimensionIndividual = m.createIndividual(ns
						+ String.format(DIMENSION_INDIVIDUAL_NAME_FORMAT,
								dimensionIdentifier),
						m.getOntClass(ContextModel.FormalDimension.getURI()));

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

				// assign label and comment to dimension
				assignLabelAndComment(dimensionIndividual, dimensionCategory);

				// parse dimension parameters
				for (int parameterIndex = 1; parameterIndex <= dimensionCategory
						.getParameters().size(); parameterIndex++) {
					// create parameter identifier
					String parameterIdentifier = createIdentifier(
							dimensionIdentifier, parameterIndex);

					Parameter parameter = dimensionCategory.getParameters()
							.get(parameterIndex - 1);

					// create parameter stuff
					Individual parameterIndividual = createParameter(parameter,
							parameterIdentifier, m, m
									.getOntClass(ContextModel.FormalParameter
											.getURI()));

					// assign label and comment to parameter
					assignLabelAndComment(parameterIndividual, parameter);

					if (config.getUseMetaModel()) {
						// set dimensionParameter property
						dimensionIndividual.addProperty(
								ContextMetaModel.dimensionParameter,
								parameterIndividual);
					}
				}

				// create values class
				OntClass valueClass = m.createClass(ns
						+ String.format(VALUE_CLASS_NAME_FORMAT,
								dimensionIdentifier));
				valueClass.addSuperClass(m.getOntClass(ContextModel.DimensionValue.getURI()));

				// add values class to set
				// definedValueClasses.add(valueClass);

				// create value assignment property
				OntProperty assignmentProperty = m.createOntProperty(ns
						+ String.format(ASSIGNMENT_PROPERTY_NAME_FORMAT,
								dimensionIdentifier));
				assignmentProperty
						.setSuperProperty(m.getObjectProperty(ContextModel.dimensionAssignmentValue.getURI()));
				assignmentProperty.setRange(valueClass);
				// we'll set domain later

				// create assignment class
				OntClass assignmentClass = m.createClass(ns
						+ String.format(ASSIGNMENT_CLASS_NAME_FORMAT,
								dimensionIdentifier));

				if (config.getComplexity() == ModelComplexityLevel.HIGH) {
					// set equivalent restrictions
					assignmentClass.addEquivalentClass(m
							.createHasValueRestriction(null,
									m.getObjectProperty(ContextModel.assignmentDimension.getURI()),
									dimensionIndividual));
					assignmentClass.addEquivalentClass(m
							.createSomeValuesFromRestriction(null,
									m.getObjectProperty(ContextModel.dimensionAssignmentValue.getURI()),
									valueClass));

					// set superclass
					RDFList intersectionList = m.createList();
					intersectionList = intersectionList.with(context
							.getDimensionAssignmentClass());
					intersectionList = intersectionList.with(m
							.createCardinalityRestriction(null,
									assignmentProperty, 1));
					if (context.getDimensionAssignmentProperty() != null)
						intersectionList = intersectionList.with(m
								.createHasValueRestriction(null, context
										.getDimensionAssignmentProperty(),
										context.getValueIndividual()));
					assignmentClass.addSuperClass(m.createIntersectionClass(
							null, intersectionList));
				} else if (config.getComplexity() == ModelComplexityLevel.LOW) {
					// set equivalent restriction
					RDFList equivalentIntersectionList = m.createList();
					equivalentIntersectionList = equivalentIntersectionList
							.with(m.createCardinalityRestriction(null,
									assignmentProperty, 1));
					equivalentIntersectionList = equivalentIntersectionList
							.with(m.createHasValueRestriction(null,
									m.getObjectProperty(ContextModel.assignmentDimension.getURI()),
									dimensionIndividual));
					assignmentClass.addEquivalentClass(m
							.createIntersectionClass(null,
									equivalentIntersectionList));

					// set superclass
					Resource superclass;
					if (context.getDimensionAssignmentProperty() != null) {
						RDFList superclassIntersectionList = m.createList();
						superclassIntersectionList = superclassIntersectionList
								.with(context.getDimensionAssignmentClass());
						superclassIntersectionList = superclassIntersectionList
								.with(m.createHasValueRestriction(null, context
										.getDimensionAssignmentProperty(),
										context.getValueIndividual()));
						superclass = m.createIntersectionClass(null,
								superclassIntersectionList);
					} else {
						superclass = context.getDimensionAssignmentClass();
					}
					assignmentClass.addSuperClass(superclass);
				}

				// add assignment class to set
				// definedAssignmentClasses.add(assignmentClass);

				// set assignment property domain
				assignmentProperty.setDomain(assignmentClass);

				// parse value categories
				for (int valueIndex = 1; valueIndex <= dimensionCategory
						.getSubCategories().size(); valueIndex++) {
					Category valueCategory = dimensionCategory
							.getSubCategories().get(valueIndex - 1);

					// create value identifier
					String valueIdentifier = createIdentifier(
							dimensionIdentifier, valueIndex);

					// create value individual
					Individual valueIndividual = m.createIndividual(ns
							+ String.format(VALUE_INDIVIDUAL_NAME_FORMAT,
									valueIdentifier), valueClass);

					// assign label and comment to value
					assignLabelAndComment(valueIndividual, valueCategory);

					if (config.getUseMetaModel()) {
						// set dimensionValue property
						dimensionIndividual.addProperty(
								ContextMetaModel.dimensionValue,
								valueIndividual);
					}

					// parse value parameters
					for (int parameterIndex = 1; parameterIndex <= valueCategory
							.getParameters().size(); parameterIndex++) {
						// create parameter identifier
						String parameterIdentifier = createIdentifier(
								valueIdentifier, parameterIndex);
						Parameter parameter = valueCategory.getParameters()
								.get(parameterIndex - 1);

						// create parameter stuff
						Individual parameterIndividual = createParameter(
								parameter, parameterIdentifier, m,
								m.getOntClass(ContextModel.FormalParameter
										.getURI()));

						// assign label and comment to parameter
						assignLabelAndComment(parameterIndividual, parameter);

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
								.getSubCategories(), assignmentClass,
								assignmentProperty, valueIndividual,
								valueIdentifier);
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

	private static Individual createParameter(Parameter p,
			String parameterIdentifier, OntModel m,
			OntClass formalParameterClass) {
		Individual parameterIndividual = m.createIndividual(
				PervADsContextModel.NS
						+ String.format(PARAMETER_INDIVIDUAL_NAME_FORMAT,
								parameterIdentifier), formalParameterClass);

		// TODO create parameter assignment with restriction on type etc.
		return parameterIndividual;
	}

	private static void assignLabelAndComment(OntResource i, Node n) {
		i.addLabel(n.getName(), n.getLang());
		if (!StringUtils.isEmpty(n.getDescription()))
			i.addComment(n.getDescription(), n.getLang());
	}

	private static String createIdentifier(String parentIdentifier, int index) {
		StringBuilder sb = new StringBuilder();
		if (!StringUtils.isEmpty(parentIdentifier)) {
			sb.append(parentIdentifier);
			sb.append("_");
		}
		sb.append(index);
		return sb.toString();
	}

	private static void p(String s) {
		System.out.println(s);
		System.out.flush();
	}
}
