package it.polimi.dei.dbgroup.pedigree.contextmodel.builder.sources;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Category;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.ICategorySource;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class UNSPSC implements ICategorySource {
	private static final String ontBaseURI = "http://a";
	private static final String codeURI = "http://www.daml.org/2004/05/unspsc/unspsc#code";
	private AnnotationProperty codeAnnotation = null;
	@Override
	public List<Category> getCategories() throws Exception {
		List<Category> categories = new ArrayList<Category>();
		File ontFile = new File("resources/unspsc/unspsc.owl");
		if(!ontFile.exists()) throw new Exception("cannt find UNSPSC ontology file " + ontFile.getAbsolutePath());
		System.out.println("Reading ontology file " + ontFile.getAbsolutePath());
		OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		ont.read(new FileInputStream(ontFile), ontBaseURI);
		System.out.println("Finding code annotation property " + codeURI);
		codeAnnotation = ont.getAnnotationProperty(codeURI);
		System.out.println("Finding base class #ProductOrService");
		OntClass productOrService = ont.getOntClass(ontBaseURI + "#ProductOrService");
		System.out.println("Enumeration #ProductOrService direct subclasses");
		ExtendedIterator<OntClass> it = productOrService.listSubClasses(true);
		parseCategories(categories, it);
		return categories;
	}
	
	private void parseCategories(List<Category> categories, ExtendedIterator<OntClass> classes) throws Exception {
		while(classes.hasNext()) {
			OntClass ontClass = classes.next();
			String className = ontClass.getLocalName();
			System.out.println("Parsing class " + className);
			String label = ontClass.getLabel(null);
			String code = null;
			if(codeAnnotation != null) {
				RDFNode codeNode = ontClass.getPropertyValue(codeAnnotation);
				if(codeNode != null) {
					code = ((Literal) codeNode).getLexicalForm();
				}
			}
			Category category = new Category();
			category.getName().setDefaultValue(className);
			category.getName().set("en", className);
			category.getDescription().setDefaultValue(label);
			category.getDescription().set("en", label);
			if(code != null) category.setId(code);
			categories.add(category);
			parseCategories(category.getSubCategories(), ontClass.listSubClasses(true));
		}
	}

	@Override
	public String getName() {
		return "unspsc";
	}
}
