package it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.util.ModelUtils;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.Organization;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervAD;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervADsModelProxy;
import it.polimi.dei.dbgroup.pedigree.pervads.model.vocabulary.PervADsModel;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;

public class OrganizationImpl extends PervADsModelEntityImpl implements
		Organization {
	private PervAD pervad;

	public OrganizationImpl(PervADsModelProxy proxy,
			Resource organizationIndividual, PervAD pervad) {
		super(proxy, organizationIndividual);
		this.pervad = pervad;
	}

	@Override
	public PervAD getPervAD() {
		return pervad;
	}

	@Override
	public String getAddress() {
		return ModelUtils.getStringProperty(getOrganizationIndividual(),
				PervADsModel.organizationAddress);
	}

	@Override
	public String getCity() {
		return ModelUtils.getStringProperty(getOrganizationIndividual(),
				PervADsModel.organizationCity);
	}

	@Override
	public List<String> getEmails() {
		return ModelUtils.listStringProperties(getOrganizationIndividual(),
				PervADsModel.organizationEmail);
	}

	@Override
	public String getName() {
		return ModelUtils.getStringProperty(getOrganizationIndividual(),
				PervADsModel.organizationName);
	}

	@Override
	public Resource getOrganizationIndividual() {
		return getResource();
	}

	@Override
	public List<String> getWebsites() {
		return ModelUtils.listStringProperties(getOrganizationIndividual(),
				PervADsModel.organizationWebsite);
	}

	@Override
	public int getZipCode() {
		return ModelUtils.getTypedProperty(getOrganizationIndividual(),
				PervADsModel.organizationZipCode, Integer.class);
	}

	@Override
	public String getZone() {
		return ModelUtils.getStringProperty(getOrganizationIndividual(),
				PervADsModel.organizationZone);
	}

}
