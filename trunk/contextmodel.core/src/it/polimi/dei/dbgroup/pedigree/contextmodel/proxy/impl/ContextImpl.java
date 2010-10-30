package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.ContextModelException;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Assignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.AssignmentDefinition;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Context;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextInstanceProxy;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Dimension;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;
import it.polimi.dei.dbgroup.pedigree.contextmodel.util.ModelUtils;
import it.polimi.dei.dbgroup.pedigree.contextmodel.util.QueryUtils;
import it.polimi.dei.dbgroup.pedigree.contextmodel.vocabulary.ContextModel;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

public class ContextImpl extends ContextInstanceEntityImpl implements Context {
	private static final String COMPATIBLE_ASSIGNMENTS_QUERY_NAME = "compatible_assignments";

	public ContextImpl(ContextInstanceProxy proxy, Resource contextIndividual) {
		super(proxy, contextIndividual);
	}

	@Override
	public Resource getContextIndividual() {
		return getResource();
	}

	@Override
	public Assignment getAssignment(String uri) {
		Assignment assignment = null;
		Resource assignmentIndividual = ModelUtils.getResourceIfExists(
				getProxy().getModel(), uri);
		if (assignmentIndividual != null) {
			// find the dimension assignment class
			Resource assignmentClass = null;
			// TODO use direct properties to check if this is the most direct
			// class of the assignment
			Model model = getProxy().getModel();

			// use a non inference model
			while (model instanceof InfModel) {
				model = ((InfModel) model).getRawModel();
			}
			for (ExtendedIterator<RDFNode> it = model.listObjectsOfProperty(
					assignmentIndividual, RDF.type); it.hasNext();) {
				RDFNode node = it.next();
				if (node.isURIResource()) {
					Resource res = (Resource) node;
					if (!res.equals(OWL.Thing) && res.hasProperty(RDF.type, OWL.Class)) {
						assignmentClass = res;
						break;
					}
				}
			}

			if (assignmentClass != null) {
				Dimension dimension = getProxy().getContextModel()
						.findDimensionByAssignmentClass(
								assignmentClass.getURI());
				if (dimension != null) {
					// get the value
					RDFNode valueNode = assignmentIndividual.getProperty(
							dimension.getAssignmentProperty()).getObject();
					if (valueNode != null && valueNode.isURIResource()) {
						Value value = getProxy().getContextModel().findValue(
								((Resource) valueNode).getURI());
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
				getContextIndividual(), ContextModel.dimensionAssignment);
		while (iterator.hasNext()) {
			RDFNode node = iterator.next();
			if (node.isURIResource()) {
				Assignment assignment = getAssignment(((Resource) node)
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
							.getURI(), getContextIndividual().getURI());
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
