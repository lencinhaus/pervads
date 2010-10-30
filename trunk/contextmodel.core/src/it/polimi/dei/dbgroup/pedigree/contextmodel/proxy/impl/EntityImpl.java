package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextModelEntity;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Entity;

import com.hp.hpl.jena.rdf.model.Resource;

public class EntityImpl implements Entity {
	private Resource resource;

	public EntityImpl(Resource resource) {
		this.resource = resource;
	}

	public Resource getResource() {
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
