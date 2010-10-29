package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import com.hp.hpl.jena.ontology.OntResource;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextModelEntity;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Entity;

public class EntityImpl implements Entity {
	private OntResource resource;
	
	public EntityImpl(OntResource resource) {
		this.resource = resource;
	}
	
	protected OntResource getResource() {
		return resource;
	}
	
	@Override
	public String getURI() {
		return resource.getURI();
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof ContextModelEntity)
			return getURI().equals(((ContextModelEntity) o).getURI());
		return super.equals(o);
	}
}
