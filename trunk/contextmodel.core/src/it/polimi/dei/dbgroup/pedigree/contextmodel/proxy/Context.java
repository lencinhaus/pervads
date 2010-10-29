package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import java.util.List;

import com.hp.hpl.jena.ontology.Individual;

public interface Context extends AssignmentSource, ContextInstanceEntity {
	public Individual getContextIndividual();
	public List<? extends Assignment> listAssignments();
	public Assignment getAssignment(String uri);
}
