package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.List;

public class Location {

	// final since location attributes should be immutable
	public final double lat;
	public final double lng;

	public Location(double lat, double lng) {
		this.lat = lat;
		this.lng = lng;
	}
	
	//finds distance from point to new point
	public double distBetweenPoints(Location newPos) {
		double result = Math.sqrt(
				Math.pow((newPos.lat - this.lat), 2) + Math.pow((newPos.lng - this.lng), 2));
		return result;
	}
	
	//Checks if point is in range of new point
	public boolean inRange(Location newPos) {
		Location pos = new Location(this.lat, this.lng);
		if (pos.distBetweenPoints(newPos) < 0.0002) {
			return true;
		}
		return false;
	}
	
	//calculates a new position from the current position 0.0003 degrees 
	//away at a direction of the input parameter degrees
	public Location newPosition(int degrees) {
		if (degrees < 0 || degrees > 350) {
			//System.out.print("Degrees out of range");
			System.exit(0);
		}
		//convert degrees to Radians
		double angle = Math.toRadians(degrees);

		// Calculate the change in latitude and longitude
		double delta_lat = 0.0003 * Math.sin(angle);
		double delta_long = 0.0003 * Math.cos(angle);

		// Calculate the nextPos by adding the change in latitude and longitude
		// to the current position
		var newPos = new Location(lat + delta_lat, lng + delta_long);

		return newPos;
	}
	
	//returns a list of 1000 coordinates along a line between start and end- 1000 enough?
	public static List<Location> pointsOnLine(Location start, Location end) {
		var points = new ArrayList<Location>();
		points.add(start);
		double DT = 0.0003;//line length
		//D = interval,the lower then less chance that any part of line will cross no fly zone
		var D = 0.0000003; //must be multiple of 0.0003
		int noSubPoints = (int)(DT / D);
		for (int i = 0; i < noSubPoints-1; i++) {
			var T = D/ DT;
			var newLng = (1 - T) * start.lng + T * end.lng;
			var newLat = (1 - T) * start.lat + T * end.lat;
			var pointOnLine = new Location(newLat, newLng);
			D += 0.0000003;
			points.add(pointOnLine);
		}
		points.add(end);
		return points;
	}
	
	//returns true if location object is within play area 
	public boolean inPlayArea() {
        return (this.lng >= -3.192473 &&
                this.lat <= 55.946233 &&
                this.lng <= -3.184319 &&
                this.lat >= 55.942617);
    }

}
