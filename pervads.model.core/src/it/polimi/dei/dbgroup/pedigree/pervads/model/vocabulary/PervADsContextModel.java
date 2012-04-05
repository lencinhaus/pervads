package it.polimi.dei.dbgroup.pedigree.pervads.model.vocabulary;

import it.polimi.dei.dbgroup.pedigree.contextmodel.vocabulary.Common;

public final class PervADsContextModel {
	public static final String URI = Common.URI_BASE
			+ "pervads/context-model.owl";
	public static final String NS = URI + "#";
	public static final String PERVADS_SPECIFICATION_URI = NS
			+ "context_pervads";
	
	// Dimension classes
	public static final String QueryDimension = NS + "QueryDimension";
	public static final String ProfileDimension = NS + "ProfileDimension";
	
	// Value classes
	public static final String AdvertiserValue = NS + "AdvertiserValue";
	public static final String UserValue = NS + "UserValue";
	
	// Dimensions
	public static final String InterestTopic = NS + "interest-topic";
	public static final String Space = NS + "space";
	public static final String Time = NS + "time";
	public static final String Age = NS + "age";
	public static final String Gender = NS + "gender";
	
	// Values
	public static final String GenderMale = NS + "gender-male";
	public static final String GenderFemale = NS + "gender-female";
	
	public static final String AgeExact = NS + "age-exact";
	public static final String AgeInterval = NS + "age-interval";
	public static final String AgeBirthday = NS + "age-birthday";
	public static final String AgeBirthdayInterval = NS  + "age-birthday-interval";
	
	public static final String SpaceExactLocation = NS + "space-exact-location";
	public static final String SpaceExactCoordinates = NS + "space-exact-coordinates";
	public static final String SpaceExactWithinLocation = NS + "space-exact-within-location";
	public static final String SpaceExactWithinHere = NS + "space-exact-within-here";
	
	public static final String TimeExact = NS + "time-exact";
	public static final String TimeInterval = NS + "time-interval";
	
	// Parameters
	public static final String ParAgeExactX = NS + "age-exact-X";
	public static final String ParAgeIntervalFrom = NS + "age-interval-from";
	public static final String ParAgeIntervalTo = NS + "age-interval-to";
	public static final String ParAgeBirthdayX = NS + "age-birthday-X";
	public static final String ParAgeBirthdayIntervalFrom = NS + "age-birthday-interval-from";
	public static final String ParAgeBirthdayIntervalTo = NS + "age-birthday-interval-to";
	
	public static final String ParSpaceExactLocationX = NS + "space-exact-location-X";
	public static final String ParSpaceExactCoordinatesLongitude = NS + "space-exact-coordinates-longitude";
	public static final String ParSpaceExactCoordinatesLatitude = NS + "space-exact-coordinates-latitude";
	public static final String ParSpaceExactWithinLocationLocation = NS + "space-exact-within-location-location";
	public static final String ParSpaceExactWithinLocationRadius = NS + "space-exact-within-location-radius";
	public static final String ParSpaceExactWithinHereRadius = NS + "space-exact-within-here-radius";
	
	public static final String ParTimeExactDate = NS + "time-exact-date";
	public static final String ParTimeIntervalFrom = NS + "time-interval-from";
	public static final String ParTimeIntervalTo = NS + "time-interval-to";
}
