package it.polimi.dei.dbgroup.pedigree.contextmodel.builder;

import java.util.ArrayList;
import java.util.List;

public class BuilderConfiguration {
	public static class IncludedModelData {
		public String path;
		public String URI;

		public IncludedModelData(String path, String URI) {
			this.path = path;
			this.URI = URI;
		}

	}

	public static final String DEFAULT_CATEGORIES_FILE_NAME = "main.xml";
	public static final String DEFAULT_OUTPUT_FILE_NAME = "context_model";
	public static final String DEFAULT_OUTPUT_LANGUAGE = "RDF/XML-ABBREV";
	public static final boolean DEFAULT_USE_META_MODEL = false;
	public static final String DEFAULT_VERSION = "0.1";
	public static final String DEFAULT_MODEL_URI = "http://example.org/context-model.owl";
	public static final String DEFAULT_SPECIFICATION_URI = "http://example.org/context-model-spec.owl";
	public static final ModelComplexityLevel DEFAULT_COMPLEXITY = ModelComplexityLevel.LOW;
	public static final boolean DEFAULT_SHOW_XML_DECLARATION = false;
	public static final boolean DEFAULT_SHOW_DOCTYPE_DECLARATION = false;
	public static final boolean DEFAULT_CREATE_TDB_STORE = false;
	public static final boolean DEFAULT_CLASSIFY = false;
	public static final boolean DEFAULT_CREATE_BACKWARD_RULESET = false;

	private String categoriesFileName = DEFAULT_CATEGORIES_FILE_NAME;
	private String outputFileName = DEFAULT_OUTPUT_FILE_NAME;
	private String outputLanguage = DEFAULT_OUTPUT_LANGUAGE;
	private boolean useMetaModel = DEFAULT_USE_META_MODEL;
	private String version = DEFAULT_VERSION;
	private String modelURI = DEFAULT_MODEL_URI;
	private String specificationURI = DEFAULT_SPECIFICATION_URI;
	private ModelComplexityLevel complexity = DEFAULT_COMPLEXITY;
	private boolean showXMLDeclaration = DEFAULT_SHOW_XML_DECLARATION;
	private boolean showDocTypeDeclaration = DEFAULT_SHOW_DOCTYPE_DECLARATION;
	private boolean createTDBStore = DEFAULT_CREATE_TDB_STORE;
	private List<IncludedModelData> includedModels = new ArrayList<IncludedModelData>();
	private boolean classify = DEFAULT_CLASSIFY;
	private boolean createBackwardRuleset = DEFAULT_CREATE_BACKWARD_RULESET;

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

	public ModelComplexityLevel getComplexity() {
		return complexity;
	}

	public void setComplexity(ModelComplexityLevel complexity) {
		this.complexity = complexity;
	}

	public boolean isShowXMLDeclaration() {
		return showXMLDeclaration;
	}

	public void setShowXMLDeclaration(boolean showXMLDeclaration) {
		this.showXMLDeclaration = showXMLDeclaration;
	}

	public boolean isShowDocTypeDeclaration() {
		return showDocTypeDeclaration;
	}

	public void setShowDocTypeDeclaration(boolean showDocTypeDeclaration) {
		this.showDocTypeDeclaration = showDocTypeDeclaration;
	}

	public boolean isCreateTDBStore() {
		return createTDBStore;
	}

	public void setCreateTDBStore(boolean createTDBStore) {
		this.createTDBStore = createTDBStore;
	}

	public List<IncludedModelData> getIncludedModels() {
		return includedModels;
	}

	public boolean shouldClassify() {
		return classify;
	}

	public void setClassify(boolean classify) {
		this.classify = classify;
	}

	public boolean shouldCreateBackwardRuleset() {
		return createBackwardRuleset;
	}

	public void setCreateBackwardRuleset(boolean createBackwardRuleset) {
		this.createBackwardRuleset = createBackwardRuleset;
	}
}
