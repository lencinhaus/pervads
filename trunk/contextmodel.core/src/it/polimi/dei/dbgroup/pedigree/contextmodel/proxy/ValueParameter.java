package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.rdf.model.Resource;

public interface ValueParameter extends ContextModelEntity {
	public Value getParentValue();

	public Resource getActualParameterClass();

	public RDFDatatype getValuesType();

	public Resource getFormalParameterIndividual();
}
