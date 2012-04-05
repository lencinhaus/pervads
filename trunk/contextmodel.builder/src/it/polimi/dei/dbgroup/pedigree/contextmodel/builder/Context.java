package it.polimi.dei.dbgroup.pedigree.contextmodel.builder;

import java.util.List;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;

public class Context {
	private List<Category> dimensionCategories;
	private OntClass actualDimensionClass;
	private Individual valueIndividual;
	private String identifier;

	public Context(List<Category> dimensionCategories,
			OntClass actualDimensionClass,
			Individual valueIndividual, String identifier) {
		super();
		this.dimensionCategories = dimensionCategories;
		this.actualDimensionClass = actualDimensionClass;
		this.valueIndividual = valueIndividual;
		this.identifier = identifier;
	}

	public List<Category> getDimensionCategories() {
		return dimensionCategories;
	}

	public OntClass getActualDimensionClass() {
		return actualDimensionClass;
	}

	public void setActualDimensionClass(OntClass actualDimensionClass) {
		this.actualDimensionClass = actualDimensionClass;
	}

	public Individual getValueIndividual() {
		return valueIndividual;
	}

	public void setValueIndividual(Individual valueIndividual) {
		this.valueIndividual = valueIndividual;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

}
