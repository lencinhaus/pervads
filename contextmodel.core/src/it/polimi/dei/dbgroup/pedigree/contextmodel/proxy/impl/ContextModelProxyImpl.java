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
	private static final String DIMENSION_BY_ACTUAL_DIMENSION_CLASS_QUERY_NAME = "dimension_by_actual_dimension_class";
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
	public Dimension findDimension(String formalDimensionIndividualUri) {
		Resource formalDimensionIndividual = ModelUtils.getResourceIfExists(model, formalDimensionIndividualUri);
		if (formalDimensionIndividual == null)
			throw new ContextModelException(
					"cannot find formal dimension individual with URI " + formalDimensionIndividualUri);

		Dimension dimension = null;
		QueryExecution qe = null;
		try {
			qe = QueryUtils.createQuery(model, DIMENSION_QUERY_NAME, formalDimensionIndividualUri);
			ResultSet rs = qe.execSelect();
			if (rs.hasNext()) {
				dimension = DimensionImpl
						.createFromFormalDimensionAndQuerySolution(this,
								formalDimensionIndividual, rs.next(), null);
				if (rs.hasNext())
					throw new ContextModelException(
							"Found more than one dimension with uri " + formalDimensionIndividualUri);
			}
		} catch (Exception ex) {
			throw new ContextModelException("Cannot read dimension", ex);
		} finally {
			if (qe != null)
				qe.close();
		}

		if (dimension == null)
			throw new ContextModelException(
					"cannot find dimension with formal dimension " + formalDimensionIndividualUri);
		return dimension;
	}

	@Override
	public Dimension findDimensionByActualDimensionClass(String actualDimensionClassUri) {
		// TODO maybe the owl class check is too much (would need inference)
		Resource actualDimensionClass = ModelUtils.getResourceWithProperty(model,
				actualDimensionClassUri, RDF.type, OWL.Class);
		if (actualDimensionClass == null)
			throw new ContextModelException(
					"cannot find assignment class with URI "
							+ actualDimensionClassUri);

		Dimension dimension = null;
		QueryExecution qe = null;
		try {
			qe = QueryUtils.createQuery(model,
					DIMENSION_BY_ACTUAL_DIMENSION_CLASS_QUERY_NAME,
					actualDimensionClassUri);
			ResultSet rs = qe.execSelect();
			if (rs.hasNext()) {
				dimension = DimensionImpl
						.createFromActualDimensionClassAndQuerySolution(this,
								actualDimensionClass, rs.next(), null);
				if (rs.hasNext())
					throw new ContextModelException(
							"Found more than one dimension with assignment class "
									+ actualDimensionClassUri);
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
							+ actualDimensionClass.getURI());
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
