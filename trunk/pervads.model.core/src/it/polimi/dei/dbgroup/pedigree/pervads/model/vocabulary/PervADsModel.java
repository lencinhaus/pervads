package it.polimi.dei.dbgroup.pedigree.pervads.model.vocabulary;

import it.polimi.dei.dbgroup.pedigree.contextmodel.vocabulary.Common;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class PervADsModel {
	private static final Model m = ModelFactory.createDefaultModel();

	private static final Resource resource(String local) {
		if (local == null)
			return m.createResource(URI);
		return m.createResource(NS + local);
	}

	private static final Property property(String local) {
		return m.createProperty(NS + local);
	}

	public static final String URI = Common.URI_BASE + "pervads/model.owl";
	public static final String NS = URI + "#";
	
	// Ontologies
	public static final Resource Ontology = resource(null);

	// Classes
	public static final Resource Offer = resource("Offer");
	public static final Resource OfferedItem = resource("OfferedItem");
	public static final Resource Organization = resource("Organization");
	public static final Resource Discount = resource("Discount");
	public static final Resource SpecialPrice = resource("SpecialPrice");
	public static final Resource Good = resource("Good");
	public static final Resource Service = resource("Service");
	public static final Resource PervAD = resource("PervAD");

	// Object properties
	public static final Property advertises = property("advertises");
	public static final Property hasAdvertiser = property("hasAdvertiser");
	public static final Property hasContext = property("hasContext");
	public static final Property isAdvertisedIn = property("isAdvertisedIn");
	public static final Property offers = property("offers");

	// Datatype properties
	public static final Property hasAttachedMedia = property("hasAttachedMedia");
	public static final Property hasAttachedVideo = property("hasAttachedVideo");
	public static final Property hasAttachedImage = property("hasAttachedImage");
	public static final Property hasDetailCoupon = property("hasDetailCoupon");
	public static final Property hasListCoupon = property("hasListCoupon");
	public static final Property currency = property("currency");
	public static final Property discount = property("discount");
	public static final Property hasTag = property("hasTag");
	public static final Property itemDescription = property("itemDescription");
	public static final Property itemName = property("itemName");
	public static final Property organizationAddress = property("organizationAddress");
	public static final Property organizationCity = property("organizationCity");
	public static final Property organizationEmail = property("organizationEmail");
	public static final Property organizationName = property("organizationName");
	public static final Property organizationWebsite = property("organizationWebsite");
	public static final Property organizationZipCode = property("organizationZipCode");
	public static final Property organizationZone = property("organizationZone");
	public static final Property price = property("price");
	public static final Property validFrom = property("validFrom");
	public static final Property validUntil = property("validUntil");
}
