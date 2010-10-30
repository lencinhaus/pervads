package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Assignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.AssignmentDefinition;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextInstanceProxy;
import it.polimi.dei.dbgroup.pedigree.contextmodel.vocabulary.ContextModel;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class AssignmentImpl extends ContextInstanceEntityImpl implements
		Assignment {
	private AssignmentDefinition definition;

	public AssignmentImpl(ContextInstanceProxy proxy,
			AssignmentDefinition definition, Resource assignmentIndividual) {
		super(proxy, assignmentIndividual);
		this.definition = definition;
	}

	@Override
	public AssignmentDefinition getDefinition() {
		return definition;
	}

	@Override
	public Resource getAssignmentIndividual() {
		return getResource();
	}

	@Override
	public boolean isCompatible(AssignmentDefinition definition) {
		return getAssignmentIndividual().hasProperty(RDF.type,
				definition.getDimension().getAssignmentClass())
				&& getAssignmentIndividual().hasProperty(
						definition.getDimension().getAssignmentProperty(),
						definition.getValue().getValueIndividual())
				&& getAssignmentIndividual().hasProperty(
						ContextModel.assignmentDimension,
						definition.getDimension()
								.getFormalDimensionIndividual());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Assignment) {
			if (obj == this)
				return true;
			Assignment assignment = (Assignment) obj;
			return definition.equals(assignment.getDefinition())
					&& getAssignmentIndividual().equals(
							assignment.getAssignmentIndividual());
		}
		return super.equals(obj);
	}

}
