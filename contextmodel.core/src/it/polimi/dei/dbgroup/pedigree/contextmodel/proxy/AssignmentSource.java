package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import java.util.List;

public interface AssignmentSource {
	public List<? extends Assignment> findCompatibleAssignments(AssignmentDefinition definition);
}
