package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;

public interface Context extends AssignmentSource, ContextInstanceEntity {
	public Resource getContextIndividual();

	public List<? extends Assignment> listAssignments();

	public Assignment getAssignment(String uri);
}
