package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextModelProxy;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ValueParameter;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Resource;

public class ValueParameterImpl extends ContextModelEntityImpl implements
		ValueParameter {
	private Value parentValue;
	private Resource actualParameterClass;
	private Resource formalParameterIndividual;
	private RDFDatatype valuesType;

	protected ValueParameterImpl(ContextModelProxy proxy,
			Resource actualParameterClass, Resource formalParameterIndividual,
			RDFDatatype valuesType, Value parentValue) {
		super(proxy, formalParameterIndividual);
		this.actualParameterClass = actualParameterClass;
		this.valuesType = valuesType;
		this.parentValue = parentValue;
	}

	@Override
	public Value getParentValue() {
		return parentValue;
	}

	@Override
	public Resource getActualParameterClass() {
		return actualParameterClass;
	}

	@Override
	public Resource getFormalParameterIndividual() {
		return formalParameterIndividual;
	}

	@Override
	public RDFDatatype getValuesType() {
		return valuesType;
	}

	public static ValueParameter createFromQuerySolution(
			ContextModelProxy proxy, QuerySolution solution, Value parentValue) {
		return new ValueParameterImpl(proxy, solution
				.getResource("actualParameterClass"), solution
				.getResource("formalParameterIndividual"),
				findDataTypeByUri(solution.getResource("valuesDataType")
						.getURI()), parentValue);
	}
	
	public static ValueParameter createFromFormalParameterAndQuerySolution(ContextModelProxy proxy, QuerySolution solution, Value parentValue, Resource formalParameterIndividual) {
		return new ValueParameterImpl(proxy, solution
				.getResource("actualParameterClass"), formalParameterIndividual,
				findDataTypeByUri(solution.getResource("valuesDataType")
						.getURI()), parentValue);
	}

	private static RDFDatatype findDataTypeByUri(String dataTypeUri) {
		return TypeMapper.getInstance().getSafeTypeByName(dataTypeUri);
	}
}
