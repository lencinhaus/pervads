package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.ContextModelException;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Assignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.AssignmentDefinition;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Context;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextInstanceProxy;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Dimension;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;
import it.polimi.dei.dbgroup.pedigree.contextmodel.util.QueryUtils;
import it.polimi.dei.dbgroup.pedigree.contextmodel.vocabulary.ContextModel;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class ContextImpl extends ContextInstanceEntityImpl implements Context {
	private static final String COMPATIBLE_ASSIGNMENTS_QUERY_NAME = "compatible_assignments";
	private Individual contextIndividual;

	public ContextImpl(ContextInstanceProxy proxy, Individual contextIndividual) {
		super(proxy, contextIndividual);
		this.contextIndividual = contextIndividual;
	}

	@Override
	public Individual getContextIndividual() {
		return contextIndividual;
	}

	@Override
	public Assignment getAssignment(String uri) {
		Assignment assignment = null;
		Individual assignmentIndividual = getProxy().getModel().getIndividual(
				uri);
		if (assignmentIndividual != null) {
			// find the dimension assignment class
			OntClass assignmentClass = null;
			for (ExtendedIterator<OntClass> it = assignmentIndividual
					.listOntClasses(true); it.hasNext();) {
				OntClass cls = it.next();
				if (!cls.isAnon()
						&& cls.hasSuperClass(ContextModel.DimensionAssignment)) {
					assignmentClass = cls;
					break;
				}
			}

			if (assignmentClass != null) {
				Dimension dimension = getProxy().getContextModel()
						.findDimensionByAssignmentClass(
								assignmentClass.getURI());
				if (dimension != null) {
					// get the value
					RDFNode valueNode = assignmentIndividual
							.getPropertyValue(dimension.getAssignmentProperty());
					if (valueNode != null && valueNode.isURIResource()) {
						Value value = getProxy().getContextModel().findValue(
								valueNode.as(Resource.class).getURI());
						if (value != null) {
							AssignmentDefinition definition = new AssignmentDefinitionImpl(
									dimension, value);
							assignment = new AssignmentImpl(getProxy(),
									definition, assignmentIndividual);
						}
					}
				}
			}
		}

		return assignment;
	}

	@Override
	public List<? extends Assignment> listAssignments() {
		List<Assignment> assignments = new ArrayList<Assignment>();
		NodeIterator iterator = getProxy().getModel().listObjectsOfProperty(
				contextIndividual, ContextModel.dimensionAssignment);
		while (iterator.hasNext()) {
			RDFNode node = iterator.next();
			if (node.isURIResource()) {
				Assignment assignment = getAssignment(node.as(Resource.class)
						.getURI());
				if (assignment != null)
					assignments.add(assignment);
			}
		}

		return assignments;
	}

	@Override
	public List<? extends Assignment> findCompatibleAssignments(
			AssignmentDefinition definition) {
		List<Assignment> assignments = new ArrayList<Assignment>();
		List<String> assignmentResourceURIs = new ArrayList<String>();
		QueryExecution qe = null;
		try {
			qe = QueryUtils.createQuery(getProxy().getModel(),
					COMPATIBLE_ASSIGNMENTS_QUERY_NAME, definition
							.getDimension().getAssignmentClass().getURI(),
					definition.getDimension().getAssignmentProperty().getURI(),
					definition.getValue().getValueIndividual().getURI(),
					definition.getDimension().getFormalDimensionIndividual()
							.getURI(), contextIndividual.getURI());
			ResultSet rs = qe.execSelect();
			while (rs.hasNext()) {
				QuerySolution sol = rs.next();
				Resource assignmentResource = sol
						.getResource("assignmentIndividual");
				assignmentResourceURIs.add(assignmentResource.getURI());
			}
		} catch (Exception ex) {
			throw new ContextModelException(
					"Cannot read compatible assignments", ex);
		} finally {
			if (qe != null)
				qe.close();
		}

		for (String assignmentResourceURI : assignmentResourceURIs) {
			Assignment assignment = getAssignment(assignmentResourceURI);
			if (assignment != null)
				assignments.add(assignment);
		}

		return assignments;
	}
}
