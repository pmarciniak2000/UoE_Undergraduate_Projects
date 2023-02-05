package uk.ac.ed.inf.heatmap;

public class Location {

	//final since location attributes should be immutable
	public final double latitude;
	public final double longitude;

	public Location(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
}
