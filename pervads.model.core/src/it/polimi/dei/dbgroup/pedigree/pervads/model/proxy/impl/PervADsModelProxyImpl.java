package it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Context;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextInstanceProxy;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextModelFactory;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextModelProxy;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervAD;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervADsModelProxy;
import it.polimi.dei.dbgroup.pedigree.pervads.model.vocabulary.PervADsModel;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class PervADsModelProxyImpl implements PervADsModelProxy {
	private ContextModelProxy contextModel;
	private ContextInstanceProxy contextInstance;
	private OntModel model;

	public PervADsModelProxyImpl(ContextModelProxy contextModel, OntModel model) {
		super();
		this.contextModel = contextModel;
		this.model = model;
		this.contextInstance = ContextModelFactory.createInstanceProxy(
				contextModel, model);
	}

	@Override
	public ContextModelProxy getContextModel() {
		return contextModel;
	}

	@Override
	public OntModel getModel() {
		return model;
	}

	@Override
	public List<? extends PervAD> listPervADs() {
		List<PervAD> pervads = new ArrayList<PervAD>();
		ExtendedIterator<Individual> iterator = model
				.listIndividuals(PervADsModel.PervAD);
		while (iterator.hasNext()) {
			Individual pervadIndividual = iterator.next();
			RDFNode contextNode = pervadIndividual
					.getPropertyValue(PervADsModel.hasContext);
			// TODO warn if pervad has no context
			if (contextNode != null && contextNode.isURIResource()) {
				Context context = contextInstance.getContext(contextNode.as(
						Resource.class).getURI());
				if (context != null) {
					PervAD pervad = new PervADImpl(this, pervadIndividual,
							context);
					pervads.add(pervad);
				}
			}
		}

		return pervads;
	}

}
