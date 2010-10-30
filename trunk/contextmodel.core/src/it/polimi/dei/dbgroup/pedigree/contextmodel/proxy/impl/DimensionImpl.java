package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.ContextModelException;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextModelProxy;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Dimension;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;
import it.polimi.dei.dbgroup.pedigree.contextmodel.util.QueryUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class DimensionImpl extends ContextModelEntityImpl implements Dimension {
	private static final String DIMENSION_PARENT_VALUE_QUERY_NAME = "dimension_parent_value";
	private Resource assignmentClass;
	private Property assignmentProperty;
	private Resource valuesClass;
	private Value parentValue;

	private DimensionImpl(ContextModelProxy proxy, Resource assignmentClass,
			Property assignmentProperty, Resource formalDimensionIndividual,
			Resource valuesClass, Value parentValue) {
		super(proxy, formalDimensionIndividual);
		this.assignmentClass = assignmentClass;
		this.assignmentProperty = assignmentProperty;
		this.valuesClass = valuesClass;
		this.parentValue = parentValue;
	}

	@Override
	public Resource getAssignmentClass() {
		return assignmentClass;
	}

	@Override
	public Property getAssignmentProperty() {
		return assignmentProperty;
	}

	@Override
	public Resource getFormalDimensionIndividual() {
		return getResource();
	}

	@Override
	public Resource getValuesClass() {
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
		ResIterator it = getProxy().getModel().listResourcesWithProperty(
				RDF.type, valuesClass);
		while (it.hasNext()) {
			Resource valueIndividual = it.next();
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
		return new DimensionImpl(proxy,
				solution.getResource("assignmentClass"), solution.getResource(
						"assignmentProperty").as(Property.class), solution
						.getResource("formalDimension"), solution
						.getResource("valuesClass"), parentValue);
	}

	public static Dimension createFromFormalDimensionAndQuerySolution(
			ContextModelProxy proxy, Resource formalDimension,
			QuerySolution solution, Value parentValue) {
		return new DimensionImpl(proxy,
				solution.getResource("assignmentClass"), solution.getResource(
						"assignmentProperty").as(Property.class),
				formalDimension, solution.getResource("valuesClass"),
				parentValue);
	}

	public static Dimension createFromAssignmentClassAndQuerySolution(
			ContextModelProxy proxy, Resource assignmentClass,
			QuerySolution solution, Value parentValue) {
		return new DimensionImpl(proxy, assignmentClass, solution.getResource(
				"assignmentProperty").as(Property.class), solution
				.getResource("formalDimension"), solution
				.getResource("valuesClass"), parentValue);
	}
}
