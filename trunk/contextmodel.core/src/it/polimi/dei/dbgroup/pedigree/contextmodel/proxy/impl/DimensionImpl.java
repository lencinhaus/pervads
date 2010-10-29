package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.ContextModelException;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextModelProxy;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Dimension;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;
import it.polimi.dei.dbgroup.pedigree.contextmodel.util.QueryUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class DimensionImpl extends ContextModelEntityImpl implements Dimension {
	private static final String DIMENSION_PARENT_VALUE_QUERY_NAME = "dimension_parent_value";
	private OntClass assignmentClass;
	private OntProperty assignmentProperty;
	private Individual formalDimensionIndividual;
	private OntClass valuesClass;
	private Value parentValue;

	private DimensionImpl(ContextModelProxy proxy, OntClass assignmentClass,
			OntProperty assignmentProperty,
			Individual formalDimensionIndividual, OntClass valuesClass,
			Value parentValue) {
		super(proxy, formalDimensionIndividual);
		this.assignmentClass = assignmentClass;
		this.assignmentProperty = assignmentProperty;
		this.formalDimensionIndividual = formalDimensionIndividual;
		this.valuesClass = valuesClass;
		this.parentValue = parentValue;
	}

	@Override
	public OntClass getAssignmentClass() {
		return assignmentClass;
	}

	@Override
	public OntProperty getAssignmentProperty() {
		return assignmentProperty;
	}

	@Override
	public Individual getFormalDimensionIndividual() {
		return formalDimensionIndividual;
	}

	@Override
	public OntClass getValuesClass() {
		return valuesClass;
	}

	@Override
	public Value getParentValue() {
		if (parentValue == null) {
			QueryExecution qe = null;
			try {
				qe = QueryUtils.createQuery(getProxy().getModel(),
						DIMENSION_PARENT_VALUE_QUERY_NAME, assignmentClass
								.getURI());
				ResultSet rs = qe.execSelect();
				if (rs.hasNext()) {
					parentValue = ValueImpl.createFromQuerySolution(getProxy(),
							rs.next(), null);
				}
			} catch (Exception ex) {
				throw new ContextModelException("Cannot parse parent value", ex);
			} finally {
				if (qe != null)
					qe.close();
			}
		}
		return parentValue;
	}

	@Override
	public Collection<? extends Value> listChildValues() {
		List<Value> values = new ArrayList<Value>();
		ExtendedIterator<Individual> valueIndividualsIterator = valuesClass
				.getOntModel().listIndividuals(valuesClass);
		while (valueIndividualsIterator.hasNext()) {
			Individual valueIndividual = valueIndividualsIterator.next();
			Value value = ValueImpl.create(getProxy(), valueIndividual, this);
			values.add(value);
		}
		return values;
	}

	@Override
	public int getDepth() {
		int depth = 0;
		Dimension dimension = this;
		while (dimension.getParentValue() != null) {
			dimension = dimension.getParentValue().getParentDimension();
			depth++;
		}
		return depth;
	}

	@Override
	public int getDistance(Dimension dimension) {
		return getDepth() - dimension.getDepth();
	}

	public static Dimension createFromQuerySolution(ContextModelProxy proxy,
			QuerySolution solution, Value parentValue) {
		return new DimensionImpl(proxy, solution.getResource("assignmentClass")
				.as(OntClass.class), solution.getResource("assignmentProperty")
				.as(OntProperty.class), solution.getResource("formalDimension")
				.as(Individual.class), solution.getResource("valuesClass").as(
				OntClass.class), parentValue);
	}

	public static Dimension createFromFormalDimensionAndQuerySolution(
			ContextModelProxy proxy, Individual formalDimension,
			QuerySolution solution, Value parentValue) {
		return new DimensionImpl(proxy, solution.getResource("assignmentClass")
				.as(OntClass.class), solution.getResource("assignmentProperty")
				.as(OntProperty.class), formalDimension, solution.getResource(
				"valuesClass").as(OntClass.class), parentValue);
	}

	public static Dimension createFromAssignmentClassAndQuerySolution(
			ContextModelProxy proxy, OntClass assignmentClass,
			QuerySolution solution, Value parentValue) {
		return new DimensionImpl(proxy, assignmentClass, solution.getResource(
				"assignmentProperty").as(OntProperty.class), solution
				.getResource("formalDimension").as(Individual.class), solution
				.getResource("valuesClass").as(OntClass.class), parentValue);
	}
}
