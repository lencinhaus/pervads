package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextEntity;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextModelProxy;

import java.util.Locale;

import com.hp.hpl.jena.ontology.OntResource;

public class ContextEntityImpl implements ContextEntity {
	private OntResource resource;
	private ContextModelProxy proxy;

	protected ContextEntityImpl(ContextModelProxy proxy, OntResource resource) {
		super();
		this.resource = resource;
		this.proxy = proxy;
	}

	@Override
	public String getDescription(String lang) {
		if (lang != null) {
			String description = resource.getComment(lang);
			if (description != null)
				return description;
		}
		return resource.getComment(null);
	}

	@Override
	public String getName(String lang) {
		if (lang != null) {
			String name = resource.getLabel(lang);
			if (name != null)
				return name;
		}
		return resource.getLabel(null);
	}

	@Override
	public String getDescription() {
		return getDescription(getDefaultLanguage());
	}

	@Override
	public String getName() {
		return getName(getDefaultLanguage());
	}

	private static String getDefaultLanguage() {
		Locale locale = Locale.getDefault();
		if (locale != null)
			return locale.getLanguage();
		return null;
	}

	@Override
	public String getURI() {
		return resource.getURI();
	}

	@Override
	public ContextModelProxy getProxy() {
		return proxy;
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof ContextEntity)
			return getURI().equals(((ContextEntity) o).getURI());
		return super.equals(o);
	}

}
