package it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.util.ModelUtils;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.Organization;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervAD;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervADsModelProxy;
import it.polimi.dei.dbgroup.pedigree.pervads.model.vocabulary.PervADsModel;

import java.util.List;

import com.hp.hpl.jena.ontology.Individual;

public class OrganizationImpl extends PervADsModelEntityImpl implements
		Organization {
	private Individual organizationIndividual;
	private PervAD pervad;

	public OrganizationImpl(PervADsModelProxy proxy,
			Individual organizationIndividual, PervAD pervad) {
		super(proxy, organizationIndividual);
		this.organizationIndividual = organizationIndividual;
		this.pervad = pervad;
	}

	@Override
	public PervAD getPervAD() {
		return pervad;
	}

	@Override
	public String getAddress() {
		return ModelUtils.parseStringLiteral(organizationIndividual.getPropertyValue(PervADsModel.organizationAddress));
	}

	@Override
	public String getCity() {
		return ModelUtils.parseStringLiteral(organizationIndividual.getPropertyValue(PervADsModel.organizationCity));
	}

	@Override
	public List<String> getEmails() {
		return ModelUtils.parseStringLiterals(organizationIndividual.listPropertyValues(PervADsModel.organizationEmail));
	}

	@Override
	public String getName() {
		return ModelUtils.parseStringLiteral(organizationIndividual.getPropertyValue(PervADsModel.organizationName));
	}

	@Override
	public Individual getOrganizationIndividual() {
		return organizationIndividual;
	}

	@Override
	public List<String> getWebsites() {
		return ModelUtils.parseStringLiterals(organizationIndividual.listPropertyValues(PervADsModel.organizationWebsite));
	}

	@Override
	public int getZipCode() {
		return ModelUtils.parseIntLiteral(organizationIndividual.getPropertyValue(PervADsModel.organizationZipCode)); 
	}

	@Override
	public String getZone() {
		return ModelUtils.parseStringLiteral(organizationIndividual.getPropertyValue(PervADsModel.organizationZone));
	}

}
