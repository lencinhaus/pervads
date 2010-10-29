package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Assignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.AssignmentDefinition;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextInstanceProxy;
import it.polimi.dei.dbgroup.pedigree.contextmodel.vocabulary.ContextModel;

import com.hp.hpl.jena.ontology.Individual;

public class AssignmentImpl extends ContextInstanceEntityImpl implements Assignment {
	private AssignmentDefinition definition;
	private Individual assignmentIndividual;

	public AssignmentImpl(ContextInstanceProxy proxy, AssignmentDefinition definition,
			Individual assignmentIndividual) {
		super(proxy, assignmentIndividual);
		this.definition = definition;
		this.assignmentIndividual = assignmentIndividual;
	}

	@Override
	public AssignmentDefinition getDefinition() {
		return definition;
	}

	@Override
	public Individual getAssignmentIndividual() {
		return assignmentIndividual;
	}

	@Override
	public boolean isCompatible(AssignmentDefinition definition) {
		return assignmentIndividual.hasOntClass(definition.getDimension()
				.getAssignmentClass())
				&& assignmentIndividual.hasProperty(definition.getDimension()
						.getAssignmentProperty(), definition.getValue()
						.getValueIndividual())
				&& assignmentIndividual.hasProperty(
						ContextModel.assignmentDimension, definition
								.getDimension().getFormalDimensionIndividual());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Assignment) {
			if(obj == this) return true;
			Assignment assignment = (Assignment) obj;
			return definition.equals(assignment.getDefinition())
					&& assignmentIndividual.equals(assignment
							.getAssignmentIndividual());
		}
		return super.equals(obj);
	}

}
