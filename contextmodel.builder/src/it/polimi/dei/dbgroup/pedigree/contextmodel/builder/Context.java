package it.polimi.dei.dbgroup.pedigree.contextmodel.builder;

import java.util.List;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;

public class Context {
	private List<Category> dimensionCategories;
	private OntClass dimensionAssignmentClass;
	private OntProperty dimensionAssignmentProperty;
	private Individual valueIndividual;
	private String identifier;

	public Context(List<Category> dimensionCategories,
			OntClass dimensionAssignmentClass,
			OntProperty dimensionAssignmentProperty,
			Individual valueIndividual, String identifier) {
		super();
		this.dimensionCategories = dimensionCategories;
		this.dimensionAssignmentClass = dimensionAssignmentClass;
		this.dimensionAssignmentProperty = dimensionAssignmentProperty;
		this.valueIndividual = valueIndividual;
		this.identifier = identifier;
	}

	public List<Category> getDimensionCategories() {
		return dimensionCategories;
	}

	public OntClass getDimensionAssignmentClass() {
		return dimensionAssignmentClass;
	}

	public void setDimensionAssignmentClass(OntClass dimensionAssignmentClass) {
		this.dimensionAssignmentClass = dimensionAssignmentClass;
	}

	public OntProperty getDimensionAssignmentProperty() {
		return dimensionAssignmentProperty;
	}

	public void setDimensionAssignmentProperty(
			OntProperty dimensionAssignmentProperty) {
		this.dimensionAssignmentProperty = dimensionAssignmentProperty;
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
