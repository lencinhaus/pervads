package it.polimi.dei.dbgroup.pedigree.contextmodel.builder;

import it.polimi.dei.dbgroup.pedigree.contextmodel.vocabulary.PervADsContextModel;

public class BuilderConfiguration {
	public static final String DEFAULT_CATEGORIES_FILE_NAME = "main.xml";
	public static final String DEFAULT_OUTPUT_FILE_NAME = "pervads";
	public static final String DEFAULT_OUTPUT_LANGUAGE = "RDF/XML-ABBREV";
	public static final boolean DEFAULT_USE_META_MODEL = false;
	public static final String DEFAULT_VERSION = "0.1";
	public static final String DEFAULT_MODEL_URI = PervADsContextModel.URI;
	public static final String DEFAULT_SPECIFICATION_URI = PervADsContextModel.PERVADS_SPECIFICATION_URI;

	private String categoriesFileName = DEFAULT_CATEGORIES_FILE_NAME;
	private String outputFileName = DEFAULT_OUTPUT_FILE_NAME;
	private String outputLanguage = DEFAULT_OUTPUT_LANGUAGE;
	private boolean useMetaModel = DEFAULT_USE_META_MODEL;
	private String version = DEFAULT_VERSION;
	private String modelURI = DEFAULT_MODEL_URI;
	private String specificationURI = DEFAULT_SPECIFICATION_URI;

	public String getCategoriesFileName() {
		return categoriesFileName;
	}

	public void setCategoriesFileName(String categoriesFileName) {
		this.categoriesFileName = categoriesFileName;
	}

	public String getOutputFileName() {
		return outputFileName;
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	public String getOutputLanguage() {
		return outputLanguage;
	}

	public void setOutputLanguage(String outputLanguage) {
		this.outputLanguage = outputLanguage;
	}

	public boolean getUseMetaModel() {
		return useMetaModel;
	}

	public void setUseMetaModel(boolean useMetaModel) {
		this.useMetaModel = useMetaModel;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getModelURI() {
		return modelURI;
	}

	public void setModelURI(String modelURI) {
		this.modelURI = modelURI;
	}

	public String getSpecificationURI() {
		return specificationURI;
	}

	public void setSpecificationURI(String specificationURI) {
		this.specificationURI = specificationURI;
	}

	
}
