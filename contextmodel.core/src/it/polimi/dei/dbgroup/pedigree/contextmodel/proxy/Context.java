package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import com.hp.hpl.jena.rdf.model.Resource;

public interface Context extends
		DimensionAssignmentSource<ActualDimensionAssignment>,
		ContextInstanceEntity {
	public Resource getContextIndividual();

	public ActualDimensionAssignment findAssignmentForDimension(
			Dimension dimension);
}
