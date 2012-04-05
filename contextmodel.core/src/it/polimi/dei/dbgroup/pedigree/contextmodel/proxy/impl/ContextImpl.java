package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.ContextModelException;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ActualDimension;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ActualDimensionAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ActualParameter;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ActualParameterAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Context;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextInstanceProxy;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Dimension;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ValueParameter;
import it.polimi.dei.dbgroup.pedigree.contextmodel.util.QueryUtils;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

public class ContextImpl extends ContextInstanceEntityImpl implements Context {
	private static final String ACTUAL_DIMENSION_QUERY_NAME = "actual_dimension";
	private static final String ACTUAL_PARAMETERS_QUERY_NAME = "actual_parameters";

	public ContextImpl(ContextInstanceProxy proxy, Resource contextIndividual) {
		super(proxy, contextIndividual);
	}

	@Override
	public Resource getContextIndividual() {
		return getResource();
	}

	// @Override
	// public Assignment getAssignment(String uri) {
	// Assignment assignment = null;
	// Resource assignmentIndividual = ModelUtils.getResourceIfExists(
	// getProxy().getModel(), uri);
	// if (assignmentIndividual != null) {
	// // find the dimension assignment class
	// Resource assignmentClass = null;
	// // TODO use direct properties to check if this is the most direct
	// // class of the assignment
	// Model model = getProxy().getModel();
	//
	// // use a non inference model
	// while (model instanceof InfModel) {
	// model = ((InfModel) model).getRawModel();
	// }
	// for (ExtendedIterator<RDFNode> it = model.listObjectsOfProperty(
	// assignmentIndividual, RDF.type); it.hasNext();) {
	// RDFNode node = it.next();
	// if (node.isURIResource()) {
	// Resource res = (Resource) node;
	// if (!res.equals(OWL.Thing) && res.hasProperty(RDF.type, OWL.Class)) {
	// assignmentClass = res;
	// break;
	// }
	// }
	// }
	//
	// if (assignmentClass != null) {
	// Dimension dimension = getProxy().getContextModel()
	// .findDimensionByActualDimensionClass(
	// assignmentClass.getURI());
	// if (dimension != null) {
	// // get the value
	// RDFNode valueNode = assignmentIndividual.getProperty(
	// dimension.getAssignmentProperty()).getObject();
	// if (valueNode != null && valueNode.isURIResource()) {
	// Value value = getProxy().getContextModel().findValue(
	// ((Resource) valueNode).getURI());
	// if (value != null) {
	// DimensionAssignment definition = new AssignmentDefinitionImpl(
	// dimension, value);
	// assignment = new AssignmentImpl(getProxy(),
	// definition, assignmentIndividual);
	// }
	// }
	// }
	// }
	// }
	//
	// return assignment;
	// }
	//
	// @Override
	// public List<? extends Assignment> listAssignments() {
	// List<Assignment> assignments = new ArrayList<Assignment>();
	// NodeIterator iterator = getProxy().getModel().listObjectsOfProperty(
	// getContextIndividual(), ContextModelVocabulary.hasDimension);
	// while (iterator.hasNext()) {
	// RDFNode node = iterator.next();
	// if (node.isURIResource()) {
	// Assignment assignment = getAssignment(((Resource) node)
	// .getURI());
	// if (assignment != null)
	// assignments.add(assignment);
	// }
	// }
	//
	// return assignments;
	// }

	// @Override
	// public List<? extends Assignment> findCompatibleAssignments(
	// DimensionAssignmentImpl definition) {
	// List<Assignment> assignments = new ArrayList<Assignment>();
	// List<String> assignmentResourceURIs = new ArrayList<String>();
	// QueryExecution qe = null;
	// try {
	// qe = QueryUtils.createQuery(getProxy().getModel(),
	// COMPATIBLE_ASSIGNMENTS_QUERY_NAME, definition
	// .getDimension().getActualDimensionClass().getURI(),
	// definition.getDimension().getAssignmentProperty().getURI(),
	// definition.getValue().getValueIndividual().getURI(),
	// definition.getDimension().getFormalDimensionIndividual()
	// .getURI(), getContextIndividual().getURI());
	// ResultSet rs = qe.execSelect();
	// while (rs.hasNext()) {
	// QuerySolution sol = rs.next();
	// Resource assignmentResource = sol
	// .getResource("assignmentIndividual");
	// assignmentResourceURIs.add(assignmentResource.getURI());
	// }
	// } catch (Exception ex) {
	// throw new ContextModelException(
	// "Cannot read compatible assignments", ex);
	// } finally {
	// if (qe != null)
	// qe.close();
	// }
	//
	// for (String assignmentResourceURI : assignmentResourceURIs) {
	// Assignment assignment = getAssignment(assignmentResourceURI);
	// if (assignment != null)
	// assignments.add(assignment);
	// }
	//
	// return assignments;
	// }

	@Override
	public ActualDimensionAssignment findAssignmentForDimension(
			final Dimension dimension) {
		ActualDimensionAssignment assignment = null;

		QueryExecution qe = null;
		try {
			qe = QueryUtils.createQuery(getProxy().getModel(),
					ACTUAL_DIMENSION_QUERY_NAME, dimension
							.getActualDimensionClass().getURI(),
					getContextIndividual().getURI(), dimension.getValuesClass()
							.getURI());
			ResultSet rs = qe.execSelect();
			if (rs.hasNext()) {
				QuerySolution sol = rs.next();
				String valueIndividualUri = sol.getResource("valueIndividual")
						.getURI();
				Resource actualDimensionIndividual = sol
						.getResource("actualDimensionIndividual");
				if (rs.hasNext())
					throw new ContextModelException(
							"Found more than one assignment for dimension "
									+ dimension);

				Value value = getProxy().getContextModel().findValue(
						valueIndividualUri);
				if (value == null)
					throw new ContextModelException(
							"Found an assignment for dimension " + dimension
									+ " to unknown value " + valueIndividualUri);
				ActualDimension actualDimension = new ActualDimensionImpl(
						getProxy(), actualDimensionIndividual);
				List<ActualParameterAssignment> parameterAssignments = new ArrayList<ActualParameterAssignment>();

				qe.close();
				qe = QueryUtils.createQuery(getProxy().getModel(),
						ACTUAL_PARAMETERS_QUERY_NAME, valueIndividualUri);
				rs = qe.execSelect();
				while (rs.hasNext()) {
					sol = rs.next();
					Resource actualParameterIndividual = sol
							.getResource("actualParameterIndividual");
					String formalParameterIndividualUri = sol.getResource(
							"formalParameterIndividual").getURI();
					Literal valueLiteral = sol.getLiteral("valueLiteral");
					ActualParameter actualParameter = new ActualParameterImpl(
							getProxy(), actualParameterIndividual);
					ValueParameter parameter = value
							.findParameter(formalParameterIndividualUri);
					if (parameter == null)
						throw new ContextModelException("parameter with uri "
								+ formalParameterIndividualUri
								+ " does not exist");
					Object parameterValue = parameter.getValuesType().parse(
							valueLiteral.getLexicalForm());
					ActualParameterAssignment parameterAssignment = new ActualParameterAssignmentImpl(
							parameter, parameterValue, actualParameter);
					parameterAssignments.add(parameterAssignment);
				}

				assignment = new ActualDimensionAssignmentImpl(value,
						actualDimension, parameterAssignments);
			}

		} catch (Exception ex) {
			throw new ContextModelException(
					"Cannot read compatible assignments", ex);
		} finally {
			if (qe != null)
				qe.close();
		}

		return assignment;
	}
}
