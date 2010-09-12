package it.polimi.dei.dbgroup.pedigree.contextmodel.builder;

import it.polimi.dei.dbgroup.pedigree.contextmodel.vocabulary.ContextModel;
import it.polimi.dei.dbgroup.pedigree.contextmodel.vocabulary.PervADsContextModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

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
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.shared.UnknownPropertyException;

public class Builder {
	public static final String CATEGORIES_FOLDER = "categories";
	public static final String MODELS_FOLDER = "models";
	public static final String OWL_FOLDER = "owl";

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
				writer.setProperty("showXMLDeclaration", "true");
			} catch (UnknownPropertyException upe) {
			}
			try {
				writer.setProperty("showDoctypeDeclaration", "true");
			} catch (UnknownPropertyException upe) {
			}
			try {
				writer.setProperty("xmlbase", PervADsContextModel.URI);
			} catch (UnknownPropertyException upe) {
			}
			writer.write(model, new FileOutputStream(modelFile),
					PervADsContextModel.URI);

			// p("Testing model...");
			// OntModel test = ModelFactory
			// .createOntologyModel(PelletReasonerFactory.THE_SPEC);
			// test.add(model);
			// Individual ind = test.createIndividual(test
			// .getOntClass(PervADsContextModel.NS
			// + "Dimension_1_10_1_Assignment"));
			// ind.addProperty(test.getOntProperty(PervADsContextModel.NS
			// + "dimension_1_10_1_AssignmentValue"), test
			// .getIndividual(PervADsContextModel.NS + "value_1_10_1_1"));
			// StmtIterator it = ind.listProperties();
			// while (it.hasNext()) {
			// p(it.next().toString());
			// }
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.flush();
		} finally {
			p("Done.");
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
			p("Reading categories from file "
					+ categoriesFile.getName());
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
		// create resource proxy
		ResourceProxy proxy = new ResourceProxy(config.getUseMetaModel());

		// create model with no reasoning
		OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);

		// set namespace prefix
		m.setNsPrefix("", PervADsContextModel.NS);
		m.setNsPrefix(CONTEXT_MODEL_NS_PREFIX, ContextModel.NS);

		// create ontology and import context model
		Ontology ontology = m.createOntology(PervADsContextModel.URI);
		ontology.addVersionInfo(config.getVersion());
		ontology.addImport(proxy.getContextModelOntology());

		if (config.getUseMetaModel()) {
			// import context meta model
			ontology.addImport(proxy.getContextMetaModelOntology());
		}

		Individual specificationIndividual = null;
		if (config.getUseMetaModel()) {
			// create context specification
			specificationIndividual = m.createIndividual(
					PervADsContextModel.PERVADS_SPECIFICATION_URI, proxy
							.getContextSpecificationClass());
			specificationIndividual.addVersionInfo("pagine_gialle_0.1");
		}

		// prepare initial context
		Context initialContext = new Context(categories, proxy
				.getDimensionAssignmentClass(), null, null, "");

		// init data structures and start parsing categories
		Set<OntClass> definedAssignmentClasses = new HashSet<OntClass>();
		Set<OntClass> definedValueClasses = new HashSet<OntClass>();
		Deque<Context> queue = new LinkedList<Context>();
		queue.add(initialContext);
		while (!queue.isEmpty()) {
			Context context = queue.poll();

			definedAssignmentClasses.clear();

			// parse dimension categories
			for (int dimensionIndex = 1; dimensionIndex <= context
					.getDimensionCategories().size(); dimensionIndex++) {
				Category dimensionCategory = context.getDimensionCategories()
						.get(dimensionIndex - 1);

				// create dimension identifier
				String dimensionIdentifier = createIdentifier(context
						.getIdentifier(), dimensionIndex);

				// create formal dimension individual
				Individual dimensionIndividual = m.createIndividual(
						PervADsContextModel.NS
								+ String.format(
										DIMENSION_INDIVIDUAL_NAME_FORMAT,
										dimensionIdentifier), proxy
								.getFormalDimensionClass());

				if (config.getUseMetaModel()) {
					// set rootDimension or valueSubDimension properties
					if (context.getValueIndividual() == null) {
						specificationIndividual.addProperty(proxy
								.getRootDimensionProperty(),
								dimensionIndividual);
					} else {
						context.getValueIndividual().addProperty(
								proxy.getValueSubDimensionProperty(),
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

					// create parameter stuff
					Individual parameterIndividual = createParameter(
							dimensionCategory.getParameters().get(
									parameterIndex - 1), parameterIdentifier,
							m, proxy.getFormalParameterClass());

					if (config.getUseMetaModel()) {
						// set dimensionParameter property
						dimensionIndividual.addProperty(proxy
								.getDimensionParameterProperty(),
								parameterIndividual);
					}
				}

				// create values class
				OntClass valueClass = m.createClass(PervADsContextModel.NS
						+ String.format(VALUE_CLASS_NAME_FORMAT,
								dimensionIdentifier));
				valueClass.addSuperClass(proxy.getDimensionValueClass());

				// add values class to set
				definedValueClasses.add(valueClass);

				// create value assignment property
				OntProperty assignmentProperty = m
						.createOntProperty(PervADsContextModel.NS
								+ String.format(
										ASSIGNMENT_PROPERTY_NAME_FORMAT,
										dimensionIdentifier));
				assignmentProperty.setSuperProperty(proxy
						.getDimensionAssignmentValueProperty());
				assignmentProperty.setRange(valueClass);
				// we'll set domain later

				// create assignment class
				OntClass assignmentClass = m.createClass(PervADsContextModel.NS
						+ String.format(ASSIGNMENT_CLASS_NAME_FORMAT,
								dimensionIdentifier));

				// set equivalent restrictions
				assignmentClass.addEquivalentClass(m.createHasValueRestriction(
						null, proxy.getAssignmentDimensionProperty(),
						dimensionIndividual));
				assignmentClass.addEquivalentClass(m
						.createSomeValuesFromRestriction(null, proxy
								.getDimensionAssignmentValueProperty(),
								valueClass));

				// set superclass
				List<RDFNode> intersectionList = new ArrayList<RDFNode>();
				intersectionList.add(context.getDimensionAssignmentClass());
				intersectionList.add(m.createCardinalityRestriction(null,
						assignmentProperty, 1));
				if (context.getDimensionAssignmentProperty() != null)
					intersectionList.add(m.createHasValueRestriction(null,
							context.getDimensionAssignmentProperty(), context
									.getValueIndividual()));
				assignmentClass.addSuperClass(m.createIntersectionClass(null, m
						.createList(intersectionList.iterator())));

				// add assignment class to set
				definedAssignmentClasses.add(assignmentClass);

				// set assignment property domain
				assignmentProperty.setDomain(assignmentClass);

				// parse value categories
				for (int valueIndex = 1; valueIndex < dimensionCategory
						.getSubCategories().size(); valueIndex++) {
					Category valueCategory = dimensionCategory
							.getSubCategories().get(valueIndex - 1);

					// create value identifier
					String valueIdentifier = createIdentifier(
							dimensionIdentifier, valueIndex);

					// create value individual
					Individual valueIndividual = m.createIndividual(
							PervADsContextModel.NS
									+ String.format(
											VALUE_INDIVIDUAL_NAME_FORMAT,
											valueIdentifier), valueClass);

					// assign label and comment to value
					assignLabelAndComment(valueIndividual, valueCategory);

					if (config.getUseMetaModel()) {
						// set dimensionValue property
						dimensionIndividual.addProperty(proxy
								.getDimensionValueProperty(), valueIndividual);
					}

					// parse value parameters
					for (int parameterIndex = 1; parameterIndex <= valueCategory
							.getParameters().size(); parameterIndex++) {
						// create parameter identifier
						String parameterIdentifier = createIdentifier(
								valueIdentifier, parameterIndex);

						// create parameter stuff
						Individual parameterIndividual = createParameter(
								valueCategory.getParameters().get(
										parameterIndex - 1),
								parameterIdentifier, m, proxy
										.getFormalParameterClass());

						if (config.getUseMetaModel()) {
							// set valueParameter property
							valueIndividual.addProperty(proxy
									.getValueParameterProperty(),
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

			// set as disjoint all child assignment classes
			OntClass firstAssignmentClass = null;
			Iterator<OntClass> it = definedAssignmentClasses.iterator();
			while (it.hasNext()) {
				if (firstAssignmentClass == null)
					firstAssignmentClass = it.next();
				else
					firstAssignmentClass.addDisjointWith(it.next());
			}
		}

		// set as disjoint all value classes
		OntClass firstValueClass = null;
		Iterator<OntClass> it = definedValueClasses.iterator();
		while (it.hasNext()) {
			if (firstValueClass == null)
				firstValueClass = it.next();
			else
				firstValueClass.addDisjointWith(it.next());
		}

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

	private static void assignLabelAndComment(Individual i, Node n) {
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
