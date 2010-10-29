package it.polimi.dei.dbgroup.pedigree.pervads.model.vocabulary;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import it.polimi.dei.dbgroup.pedigree.contextmodel.vocabulary.Common;

public class PervADsModel {
	private static final OntModel m = ModelFactory
			.createOntologyModel(OntModelSpec.OWL_DL_MEM);

	private static final OntClass clazz(String local) {
		return m.createClass(NS + local);
	}

	private static final ObjectProperty obj(String local) {
		return m.createObjectProperty(NS + local);
	}

	private static final DatatypeProperty data(String local) {
		return m.createDatatypeProperty(NS + local);
	}

	public static final String URI = Common.URI_BASE + "pervads/model.owl";
	public static final String NS = URI + "#";
	
	// Ontologies
	public static final Ontology Ontology = m.createOntology(URI);

	// Classes
	public static final OntClass Offer = clazz("Offer");
	public static final OntClass OfferedItem = clazz("OfferedItem");
	public static final OntClass Organization = clazz("Organization");
	public static final OntClass Discount = clazz("Discount");
	public static final OntClass SpecialPrice = clazz("SpecialPrice");
	public static final OntClass Good = clazz("Good");
	public static final OntClass Service = clazz("Service");
	public static final OntClass PervAD = clazz("PervAD");

	// Object properties
	public static final ObjectProperty advertises = obj("advertises");
	public static final ObjectProperty hasAdvertiser = obj("hasAdvertiser");
	public static final ObjectProperty hasContext = obj("hasContext");
	public static final ObjectProperty isAdvertisedIn = obj("isAdvertisedIn");
	public static final ObjectProperty offers = obj("offers");

	// Datatype properties
	public static final DatatypeProperty hasAttachedMedia = data("hasAttachedMedia");
	public static final DatatypeProperty hasAttachedVideo = data("hasAttachedVideo");
	public static final DatatypeProperty hasAttachedImage = data("hasAttachedImage");
	public static final DatatypeProperty hasDetailCoupon = data("hasDetailCoupon");
	public static final DatatypeProperty hasListCoupon = data("hasListCoupon");
	public static final DatatypeProperty currency = data("currency");
	public static final DatatypeProperty discount = data("discount");
	public static final DatatypeProperty hasTag = data("hasTag");
	public static final DatatypeProperty itemDescription = data("itemDescription");
	public static final DatatypeProperty itemName = data("itemName");
	public static final DatatypeProperty organizationAddress = data("organizationAddress");
	public static final DatatypeProperty organizationCity = data("organizationCity");
	public static final DatatypeProperty organizationEmail = data("organizationEmail");
	public static final DatatypeProperty organizationName = data("organizationName");
	public static final DatatypeProperty organizationWebsite = data("organizationWebsite");
	public static final DatatypeProperty organizationZipCode = data("organizationZipCode");
	public static final DatatypeProperty organizationZone = data("organizationZone");
	public static final DatatypeProperty price = data("price");
	public static final DatatypeProperty validFrom = data("validFrom");
	public static final DatatypeProperty validUntil = data("validUntil");
}
