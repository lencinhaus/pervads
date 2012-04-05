package it.polimi.dei.dbgroup.pedigree.pervads.model.matching.specialized;

import it.polimi.dei.dbgroup.pedigree.contextmodel.matching.specialized.TypedMatcher;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.DimensionAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ParameterAssignment;
import it.polimi.dei.dbgroup.pedigree.pervads.model.matching.LocationService;
import it.polimi.dei.dbgroup.pedigree.pervads.model.matching.specialized.SpaceValue.Type;

public class SpaceMatcher<S extends DimensionAssignment<? extends ParameterAssignment>, T extends DimensionAssignment<? extends ParameterAssignment>>
		extends TypedMatcher<S, T, SpaceValue> {

	private LocationService locationService;

	public SpaceMatcher(LocationService locationService) {
		super(SpaceValue.class);
		this.locationService = locationService;
	}

	@Override
	protected double matchValues(SpaceValue sourceValue, SpaceValue targetValue) {
		Coordinates sourceCoordinates, targetCoordinates;
		try {
			sourceCoordinates = sourceValue.getCoordinates(locationService);
			targetCoordinates = targetValue.getCoordinates(locationService);
		} catch (Exception ex) {
			throw new RuntimeException(
					"cannot read coordinates for space value", ex);
		}

		Type sourceType = sourceValue.getType();
		Type targetType = targetValue.getType();

		if (sourceType != targetType) {
			if (sourceType == Type.POSITION)
				return sourceCoordinates.relativeDistance(targetCoordinates,
						targetValue.radius);
			else
				return targetCoordinates.relativeDistance(sourceCoordinates,
						sourceValue.radius);
		} else {
			if (sourceType == Type.POSITION) {
				return sourceCoordinates.equals(targetCoordinates) ? 1D : 0D;
			} else {
				// two areas
				return Coordinates.relativeIntersection(sourceCoordinates,
						sourceValue.radius, targetCoordinates,
						targetValue.radius);
			}
		}
	}
}
