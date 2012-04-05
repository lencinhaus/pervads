package it.polimi.dei.dbgroup.pedigree.pervads.model.matching.specialized;

public class Coordinates {
	private double latitude;
	private double longitude;
	private static final double EARTH_RADIUS_KM = 6371;

	public Coordinates(double latitude, double longitude) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Coordinates) {
			Coordinates coordinates = (Coordinates) obj;
			return latitude == coordinates.latitude
					&& longitude == coordinates.longitude;
		}
		return super.equals(obj);
	}

	public double distance(Coordinates coordinates) {
		// haversine formula, see http://www.movable-type.co.uk/scripts/latlong.html
		double dLat = Math.sin(toRadians(coordinates.latitude - latitude) / 2);
		double dLon = Math
				.sin(toRadians(coordinates.longitude - longitude) / 2);
		double a = dLat * dLat + Math.cos(toRadians(latitude))
				* Math.cos(toRadians(coordinates.latitude)) * dLon * dLon;
		a = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return EARTH_RADIUS_KM * a;
	}

	public double relativeDistance(Coordinates coordinates, double radius) {
		double distance = distance(coordinates);
		if (distance > radius)
			return 0D;
		else
			return 1 - (distance / radius);
	}

	private static double toRadians(double v) {
		return v * Math.PI / 180;
	}

	public static double relativeIntersection(Coordinates c1, double r1,
			Coordinates c2, double r2) {
		/*
		 * this function is designed in order to obtain the following results:
		 * - if the two circles are disjoint, return 0
		 * - if circle2 is contained in circle1, return 1
		 * - otherwise, return the ratio between the intersection of the two circles and the area of circle2
		 * (this last condition ensures that if circle2 is way bigger that circle1 it is penalized, because
		 * circle2 belongs to the target context)
		 */
		double d = c1.distance(c2);
		if (d >= r1 + r2)
			return 0D;
		if (d <= r1 - r2)
			return 1D;
		
		double intersection;
		if(d <= r2 - r1) intersection = Math.PI * r1 * r1;
		// taken from http://mathworld.wolfram.com/Circle-CircleIntersection.html
		else intersection = r1
				* r1
				* Math.acos((d * d + r1 * r1 - r2 * r2) / (2 * d * r1))
				+ r2
				* r2
				* Math.acos((d * d + r2 * r2 - r1 * r1) / (2 * d * r2))
				- .5
				* Math.sqrt((-d + r1 + r2) * (d + r1 - r2) * (d - r1 + r2)
						* (d + r1 + r2));

		return intersection / (Math.PI * r2 * r2);
	}
}
