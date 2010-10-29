package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.NamedEntity;

import java.util.Locale;

import com.hp.hpl.jena.ontology.OntResource;

public class NamedEntityImpl extends EntityImpl implements NamedEntity {
	public NamedEntityImpl(OntResource resource) {
		super(resource);
	}

	@Override
	public String getDescription(String lang) {
		if (lang != null) {
			String description = getResource().getComment(lang);
			if (description != null)
				return description;
		}
		return getResource().getComment(null);
	}

	@Override
	public String getName(String lang) {
		if (lang != null) {
			String name = getResource().getLabel(lang);
			if (name != null)
				return name;
		}
		return getResource().getLabel(null);
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

}
