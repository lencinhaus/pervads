package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.ContextModelException;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextModelProxy;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Dimension;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;

public class ContextModelProxyImpl implements
		ContextModelProxy {
	private static final String ROOT_DIMENSIONS_QUERY_NAME = "root_dimensions";
	private static final String DIMENSION_QUERY_NAME = "dimension";
	private static final String ALL_DIMENSIONS_QUERY_NAME = "all_dimensions";
	private static final String ALL_VALUES_QUERY_NAME = "all_values";
	private OntModel model;

	public ContextModelProxyImpl(OntModel model) {
		this.model = model;
	}

	@Override
	public Collection<? extends Dimension> findAllDimensions() {
		List<Dimension> dimensions = new ArrayList<Dimension>();
		QueryExecution qe = null;
		try {
			qe = QueryUtils.createQuery(model, ALL_DIMENSIONS_QUERY_NAME);
			ResultSet rs = qe.execSelect();
			while (rs.hasNext()) {
				Dimension dimension = DimensionImpl.createFromQuerySolution(this, rs
						.next(), null);
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
				Value value = ValueImpl.createFromQuerySolution(this, rs.next(), null);
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
		Individual formalDimension = model.getIndividual(uri);
		if(uri == null) throw new ContextModelException("cannot find formal dimension individual with URI " + uri);
		
		Dimension dimension = null;
		QueryExecution qe = null;
		try {
			qe = QueryUtils.createQuery(model,
					DIMENSION_QUERY_NAME, uri);
			ResultSet rs = qe.execSelect();
			if(rs.hasNext()) {
				dimension = DimensionImpl
						.createFromFormalDimensionAndQuerySolution(this, formalDimension, rs.next(), null);
			}
		} catch (Exception ex) {
			throw new ContextModelException("Cannot read dimension",
					ex);
		} finally {
			if (qe != null)
				qe.close();
		}
		
		if(dimension == null) throw new ContextModelException("cannot find dimension with formal dimension " + uri);
		return dimension;
	}



	@Override
	public Value findValue(String uri) {
		Individual valueIndividual = model.getIndividual(uri);
		if(valueIndividual == null) throw new ContextModelException("cannot find value individual with URI " + uri);
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
				Dimension dimension = DimensionImpl.createFromQuerySolution(this, rs
						.next(), null);
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
