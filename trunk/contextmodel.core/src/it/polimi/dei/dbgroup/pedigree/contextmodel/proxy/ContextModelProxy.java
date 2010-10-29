package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import java.util.Collection;

import com.hp.hpl.jena.ontology.OntModel;

public interface ContextModelProxy extends DimensionParent {
	public OntModel getModel();
	
	public Dimension findDimension(String uri);
	
	public Dimension findDimensionByAssignmentClass(String assignmentClassUri);

	public Value findValue(String uri);

	public Collection<? extends Dimension> findAllDimensions();

	public Collection<? extends Value> findAllValues();
}
