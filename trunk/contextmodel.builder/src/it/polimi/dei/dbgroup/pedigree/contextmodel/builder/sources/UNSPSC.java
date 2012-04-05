package it.polimi.dei.dbgroup.pedigree.contextmodel.builder.sources;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Category;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.ICategorySource;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class UNSPSC implements ICategorySource {
	private static final String ontBaseURI = "http://a";
	private static final String codeURI = "http://www.daml.org/2004/05/unspsc/unspsc#code";
	private AnnotationProperty codeAnnotation = null;
	private static final Set<String> classesWithoutCode = new HashSet<String>();
	static {
		classesWithoutCode.add("Product");
		classesWithoutCode.add("Service");
	}
	
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
		if(codeAnnotation == null) throw new RuntimeException("Cannot find code annotation property " + codeURI);
		System.out.println("Finding base class #ProductOrService");
		OntClass productOrService = ont.getOntClass(ontBaseURI + "#ProductOrService");
		System.out.println("Enumeration #ProductOrService direct subclasses");
		ExtendedIterator<OntClass> it = productOrService.listSubClasses(true);
		parseCategories(categories, it, "");
		return categories;
	}
	
	private void parseCategories(List<Category> categories, ExtendedIterator<OntClass> classes, String parentCode) throws Exception {
		while(classes.hasNext()) {
			OntClass ontClass = classes.next();
			String className = ontClass.getLocalName();
			System.out.println("Parsing class " + className);
			String label = ontClass.getLabel(null);
			if(StringUtils.isEmpty(label)) label = className;
			String code = null;
			NodeIterator nodeIt = ontClass.listPropertyValues(codeAnnotation);
			String nextParentCode = new String(parentCode);
			while(nodeIt.hasNext()) {
				String foundCode = ((Literal) nodeIt.next()).getLexicalForm();
				if(!StringUtils.isEmpty(foundCode)) {
					if(foundCode.startsWith(parentCode)) {
						code = foundCode;
						nextParentCode = foundCode.substring(0, Math.min(6, parentCode.length() + 2));
						break;
					}
				}
			}
			if(StringUtils.isEmpty(code)) {
				if(classesWithoutCode.contains(className)) code = className;
				else throw new IllegalArgumentException("Class " + className + " has no code attribute");
			}
			Category category = new Category();
			category.setId(code);
			category.getName().setDefaultValue(label);
			category.getName().set("en", label);
			categories.add(category);
			ExtendedIterator<OntClass> subClasses = ontClass.listSubClasses(true);
			if(subClasses.hasNext()) {
				Category kindOfCategory = new Category();
				kindOfCategory.setId(code + "-kind");
				kindOfCategory.getName().setDefaultValue("Kind of " + label);
				kindOfCategory.getName().set("en", "Kind of " + label);
				category.getSubCategories().add(kindOfCategory);
				parseCategories(kindOfCategory.getSubCategories(), subClasses, nextParentCode);
			}
		}
	}

	@Override
	public String getName() {
		return "unspsc";
	}
}
