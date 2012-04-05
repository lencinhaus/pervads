package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Entity;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class EntityImpl implements Entity {
	private Resource resource;

	public EntityImpl(Resource resource) {
		this.resource = resource;
	}

	@Override
	public Resource getResource() {
		return resource;
	}

	@Override
	public String getURI() {
		return resource.getURI();
	}

	@Override
	public boolean hasClass(String classURI) {
		return resource.hasProperty(RDF.type, resource.getModel().createResource(classURI));
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof Entity)
			return getURI().equals(((Entity) o).getURI());
		return super.equals(o);
	}

	@Override
	public String toString() {
		return "<" + getURI() + ">";
	}
}
