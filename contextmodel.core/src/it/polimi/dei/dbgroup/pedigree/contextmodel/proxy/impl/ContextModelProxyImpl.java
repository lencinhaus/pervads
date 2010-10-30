package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.ContextModelException;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextModelProxy;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Dimension;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;
import it.polimi.dei.dbgroup.pedigree.contextmodel.util.ModelUtils;
import it.polimi.dei.dbgroup.pedigree.contextmodel.util.QueryUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

public class ContextModelProxyImpl implements ContextModelProxy {
	private static final String ROOT_DIMENSIONS_QUERY_NAME = "root_dimensions";
	private static final String DIMENSION_QUERY_NAME = "dimension";
	private static final String DIMENSION_BY_ASSIGNMENT_CLASS_QUERY_NAME = "dimension_by_assignment_class";
	private static final String ALL_DIMENSIONS_QUERY_NAME = "all_dimensions";
	private static final String ALL_VALUES_QUERY_NAME = "all_values";
	private Model model;

	public ContextModelProxyImpl(Model model) {
		this.model = model;
	}

	@Override
	public Model getModel() {
		return model;
	}

	@Override
	public Collection<? extends Dimension> findAllDimensions() {
		List<Dimension> dimensions = new ArrayList<Dimension>();
		QueryExecution qe = null;
		try {
			qe = QueryUtils.createQuery(model, ALL_DIMENSIONS_QUERY_NAME);
			ResultSet rs = qe.execSelect();
			while (rs.hasNext()) {
				Dimension dimension = DimensionImpl.createFromQuerySolution(
						this, rs.next(), null);
				dimensions.add(dimension);
			}
		} catch (Exception ex) {
			throw new ContextModelException("Cannot parse all dimensions", ex);
		} finally {
			if (qe != null)
				qe.close();
		}

		return dimensions;
	}

	@Override
	public Collection<? extends Value> findAllValues() {
		List<Value> values = new ArrayList<Value>();
		QueryExecution qe = null;
		try {
			qe = QueryUtils.createQuery(model, ALL_VALUES_QUERY_NAME);
			ResultSet rs = qe.execSelect();
			while (rs.hasNext()) {
				Value value = ValueImpl.createFromQuerySolution(this,
						rs.next(), null);
				values.add(value);
			}
		} catch (Exception ex) {
			throw new ContextModelException("Cannot parse all values", ex);
		} finally {
			if (qe != null)
				qe.close();
		}

		return values;
	}

	@Override
	public Dimension findDimension(String uri) {
		Resource formalDimension = ModelUtils.getResourceIfExists(model, uri);
		if (formalDimension == null)
			throw new ContextModelException(
					"cannot find formal dimension individual with URI " + uri);

		Dimension dimension = null;
		QueryExecution qe = null;
		try {
			qe = QueryUtils.createQuery(model, DIMENSION_QUERY_NAME, uri);
			ResultSet rs = qe.execSelect();
			if (rs.hasNext()) {
				dimension = DimensionImpl
						.createFromFormalDimensionAndQuerySolution(this,
								formalDimension, rs.next(), null);
				if (rs.hasNext())
					throw new ContextModelException(
							"Found more than one dimension with uri " + uri);
			}
		} catch (Exception ex) {
			throw new ContextModelException("Cannot read dimension", ex);
		} finally {
			if (qe != null)
				qe.close();
		}

		if (dimension == null)
			throw new ContextModelException(
					"cannot find dimension with formal dimension " + uri);
		return dimension;
	}

	@Override
	public Dimension findDimensionByAssignmentClass(String assignmentClassUri) {
		// TODO maybe the owl class check is too much (would need inference)
		Resource assignmentClass = ModelUtils.getResourceWithProperty(model,
				assignmentClassUri, RDF.type, OWL.Class);
		if (assignmentClass == null)
			throw new ContextModelException(
					"cannot find assignment class with URI "
							+ assignmentClassUri);

		Dimension dimension = null;
		QueryExecution qe = null;
		try {
			qe = QueryUtils.createQuery(model,
					DIMENSION_BY_ASSIGNMENT_CLASS_QUERY_NAME,
					assignmentClassUri);
			ResultSet rs = qe.execSelect();
			if (rs.hasNext()) {
				dimension = DimensionImpl
						.createFromAssignmentClassAndQuerySolution(this,
								assignmentClass, rs.next(), null);
				if (rs.hasNext())
					throw new ContextModelException(
							"Found more than one dimension with assignment class "
									+ assignmentClassUri);
			}
		} catch (Exception ex) {
			throw new ContextModelException("Cannot read dimension", ex);
		} finally {
			if (qe != null)
				qe.close();
		}

		if (dimension == null)
			throw new ContextModelException(
					"cannot find dimension with assignment class "
							+ assignmentClass.getURI());
		return dimension;
	}

	@Override
	public Value findValue(String uri) {
		Resource valueIndividual = ModelUtils.getResourceIfExists(model, uri);
		if (valueIndividual == null)
			throw new ContextModelException(
					"cannot find value individual with URI " + uri);
		return ValueImpl.create(this, valueIndividual, null);
	}

	@Override
	public Collection<? extends Dimension> listChildDimensions() {
		List<Dimension> dimensions = new ArrayList<Dimension>();
		QueryExecution qe = null;
		try {
			qe = QueryUtils.createQuery(model, ROOT_DIMENSIONS_QUERY_NAME);
			ResultSet rs = qe.execSelect();
			while (rs.hasNext()) {
				Dimension dimension = DimensionImpl.createFromQuerySolution(
						this, rs.next(), null);
				dimensions.add(dimension);
			}
		} catch (Exception ex) {
			throw new ContextModelException("Cannot parse root dimensions", ex);
		} finally {
			if (qe != null)
				qe.close();
		}

		return dimensions;
	}
}
