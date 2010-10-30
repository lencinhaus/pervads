package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.NamedEntity;
import it.polimi.dei.dbgroup.pedigree.contextmodel.util.ModelUtils;

import java.util.Locale;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

public class NamedEntityImpl extends EntityImpl implements NamedEntity {
	private Property nameProperty;
	private Property descriptionProperty;

	public NamedEntityImpl(Resource resource) {
		this(resource, RDFS.label, RDFS.comment);
	}

	public NamedEntityImpl(Resource resource, Property nameProperty,
			Property descriptionProperty) {
		super(resource);
		this.nameProperty = nameProperty;
		this.descriptionProperty = descriptionProperty;
	}

	@Override
	public String getDescription(String lang) {
		return ModelUtils.getStringProperty(getResource(), descriptionProperty,
				lang);
	}

	@Override
	public String getName(String lang) {
		return ModelUtils.getStringProperty(getResource(), nameProperty, lang);
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
