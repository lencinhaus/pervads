package it.polimi.dei.dbgroup.pedigree.pervads.model.matching.specialized;

import it.polimi.dei.dbgroup.pedigree.contextmodel.matching.specialized.ParameterMapping;
import it.polimi.dei.dbgroup.pedigree.contextmodel.matching.specialized.ValueMapping;
import it.polimi.dei.dbgroup.pedigree.contextmodel.matching.specialized.ValueParameterMapping;
import it.polimi.dei.dbgroup.pedigree.contextmodel.matching.specialized.ValueUriMapping;
import it.polimi.dei.dbgroup.pedigree.pervads.model.matching.LocationService;
import it.polimi.dei.dbgroup.pedigree.pervads.model.vocabulary.PervADsContextModel;

@ValueMapping(valueURIs = { PervADsContextModel.SpaceExactCoordinates,
		PervADsContextModel.SpaceExactLocation,
		PervADsContextModel.SpaceExactWithinHere,
		PervADsContextModel.SpaceExactWithinLocation })
public class SpaceValue {
	public static enum Type {
		POSITION, AREA
	}

	@ValueUriMapping
	public String valueUri;

	@ParameterMapping(valueParameters = @ValueParameterMapping(valueUri = PervADsContextModel.SpaceExactCoordinates, parameterUri = PervADsContextModel.ParSpaceExactCoordinatesLatitude))
	public double latitude;

	@ParameterMapping(valueParameters = @ValueParameterMapping(valueUri = PervADsContextModel.SpaceExactCoordinates, parameterUri = PervADsContextModel.ParSpaceExactCoordinatesLongitude))
	public double longitude;

	@ParameterMapping(valueParameters = {
			@ValueParameterMapping(valueUri = PervADsContextModel.SpaceExactLocation, parameterUri = PervADsContextModel.ParSpaceExactLocationX),
			@ValueParameterMapping(valueUri = PervADsContextModel.SpaceExactWithinLocation, parameterUri = PervADsContextModel.ParSpaceExactWithinLocationLocation) })
	public String location;

	@ParameterMapping(valueParameters = {
			@ValueParameterMapping(valueUri = PervADsContextModel.SpaceExactWithinHere, parameterUri = PervADsContextModel.ParSpaceExactWithinHereRadius),
			@ValueParameterMapping(valueUri = PervADsContextModel.SpaceExactWithinLocation, parameterUri = PervADsContextModel.ParSpaceExactWithinLocationRadius) })
	public double radius;

	private Type type = null;
	private Coordinates coordinates = null;

	public Coordinates getCoordinates(LocationService locationService)
			throws Exception {
		if (coordinates == null) {
			if (PervADsContextModel.SpaceExactCoordinates.equals(valueUri)) {
				coordinates = new Coordinates(latitude, longitude);
			} else if (PervADsContextModel.SpaceExactWithinHere
					.equals(valueUri)) {
				coordinates = locationService.getCurrentPosition();
			} else {
				coordinates = locationService.geocode(location);
			}
		}
		return coordinates;
	}

	public Type getType() {
		if (type == null) {
			if (PervADsContextModel.SpaceExactCoordinates.equals(valueUri)
					|| PervADsContextModel.SpaceExactLocation.equals(valueUri))
				type = Type.POSITION;
			else
				type = Type.AREA;
		}
		return type;
	}
}
