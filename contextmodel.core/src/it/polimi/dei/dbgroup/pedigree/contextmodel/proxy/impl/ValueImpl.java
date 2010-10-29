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
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class ValueImpl extends
		ContextModelEntityImpl implements
		Value {
	private static final String VALUE_SUB_DIMENSIONS_QUERY_NAME = "value_sub_dimensions";
	private static final String VALUE_PARENT_DIMENSION_QUERY_NAME = "value_parent_dimension";
	private Dimension parentDimension;
	private Individual valueIndividual;

	private ValueImpl(ContextModelProxy proxy, Individual valueIndividual, Dimension parentDimension) {
		super(proxy, valueIndividual);
		this.valueIndividual = valueIndividual;
		this.parentDimension = parentDimension;
	}

	@Override
	public Collection<? extends Dimension> listChildDimensions() {
		List<Dimension> dimensions = new ArrayList<Dimension>();
		QueryExecution qe = null;
		try {
			qe = QueryUtils.createQuery(getProxy().getModel(),
					VALUE_SUB_DIMENSIONS_QUERY_NAME, getParentDimension()
							.getAssignmentClass().getURI(), valueIndividual
							.getURI(), getParentDimension().getAssignmentProperty()
							.getURI());
			ResultSet rs = qe.execSelect();
			while (rs.hasNext()) {
				Dimension dimension = DimensionImpl
						.createFromQuerySolution(getProxy(), rs.next(), this);
				dimensions.add(dimension);
			}
		} catch (Exception ex) {
			throw new ContextModelException("Cannot read value subdimensions",
					ex);
		} finally {
			if (qe != null)
				qe.close();
		}
		
		return dimensions;
	}


	@Override
	public Dimension getParentDimension() {
		if(parentDimension == null) {
			QueryExecution qe = null;
			try {
				qe = QueryUtils.createQuery(getProxy().getModel(), VALUE_PARENT_DIMENSION_QUERY_NAME, valueIndividual.getURI());
				ResultSet rs = qe.execSelect();
				if(rs.hasNext()) {
					parentDimension = DimensionImpl.createFromQuerySolution(getProxy(), rs.next(), null);
				}
				else throw new Exception("value parent dimension query returned no results");
			} catch (Exception ex) {
				throw new ContextModelException("Cannot parse parent dimension", ex);
			} finally {
				if (qe != null)
					qe.close();
			}
		}
		return parentDimension;
	}


	@Override
	public Individual getValueIndividual() {
		return valueIndividual;
	}
	
	@Override
	public Dimension getRootDimension() {
		Dimension rootDimension = getParentDimension();
		while (rootDimension.getParentValue() != null)
			rootDimension = rootDimension.getParentValue().getParentDimension();
		return rootDimension;
	}

	public static Value create(ContextModelProxy proxy, Individual valueIndividual,
			Dimension parentDimension) {
		return new ValueImpl(proxy, valueIndividual, parentDimension);
	}
	
	public static Value createFromQuerySolution(ContextModelProxy proxy, QuerySolution sol, Dimension parentDimension) {
		return create(proxy, sol.getResource("valueIndividual").as(Individual.class), parentDimension);
	}
}
