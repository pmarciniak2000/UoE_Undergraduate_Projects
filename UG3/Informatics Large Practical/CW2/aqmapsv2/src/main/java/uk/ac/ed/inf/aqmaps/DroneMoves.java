package uk.ac.ed.inf.aqmaps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.turf.TurfJoins;

public class DroneMoves extends Drone {
	// recentrecentDirs hold list of directions taken by the drone since it was last
	// stuck
	private ArrayList<Integer> recentDirs = new ArrayList<Integer>();
	// recentDirs is a final list of all the possible directions the drone can take
	private final int[] direction = { 0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150, 160, 170,
			180, 190, 200, 210, 220, 230, 240, 250, 260, 270, 280, 290, 300, 310, 320, 330, 340, 350 };

	public DroneMoves(Location currPos) {
		super(currPos);
	}

	// creates a deep copy of the list of sensors
	private List<Sensor> copySensorList() {
		var sensorCopy = new ArrayList<Sensor>();
		for (var sensor : ServerParser.getSensors()) {
			sensorCopy.add(sensor);
		}
		return sensorCopy;
	}

	// Finds closest sensor from input list to input position
	private Sensor getNearestSensor(List<Sensor> leftToVisit) throws IOException, InterruptedException {
		Sensor nearestSensor = leftToVisit.get(0);
		for (var sensor : leftToVisit) {
			// basic finding minimum algorithm, if distance to next sensor less than
			// distance
			// to current sensor, next sensor becomes closest sensor, then repeat for all
			// sensors in list
			if (currPos.distBetweenPoints(ServerParser.wordsToCoords.get(sensor.getLocation())) < (currPos
					.distBetweenPoints(ServerParser.wordsToCoords.get(nearestSensor.getLocation())))) {
				nearestSensor = sensor;
			}
		}
		return nearestSensor;
	}

	// Finds optimal direction from the current position which results in a position
	// closest to the sensor position
	private int optimalDirection(Location targetPos, boolean stuck) throws IOException, InterruptedException {
		// first pick first legal direction
		var safeDirs = getSafeDirections(stuck);
		int bestDir = safeDirs.get(0);
		//System.out.println(safeDirs + " " + moves);
		double dist = currPos.distBetweenPoints(targetPos);
		// finds which dir, results in a path closest to the sensor and returns that
		// direction
		for (int dir : safeDirs) {
			Location newPos = currPos.newPosition(dir);
			double newDist = newPos.distBetweenPoints(targetPos);
			if (newDist < dist) {
				dist = newDist;
				bestDir = dir;
			}
		}
		//System.out.println("bestDir: " + bestDir +  "at move: " + " " + moves);
		return bestDir;
	}

	private ArrayList<Integer> getSafeDirections(boolean stuck) {
		var safeDirs = new ArrayList<Integer>();
		for (int d : direction) {
			Location newPos = currPos.newPosition(d);
			if (!stuck) {
				if (isLegal(currPos, newPos) && newPos.inPlayArea() && d != oppDir(lastDir)) {
					safeDirs.add(d);
				}
			} else if (stuck) {
				//System.out.println(recentDirs + " " + moves);
				if (isLegal(currPos, newPos) && newPos.inPlayArea() && !recentDirs.contains(d)
						&& d != oppDir(lastDir)) {
					safeDirs.add(d);
				}
			}

		}
		//System.out.println(safeDirs + " " + moves);
		return safeDirs;
	}

	// main drone algorithm adds simulates drone movement and adds flight details to
	// the Drone classes flighDetails variable
	public void flight() throws IOException, InterruptedException {
		var start = currPos;// save initial start position to try go back to it
		var finished = false;
		var feats = new ArrayList<Feature>();
		var leftToVisit = copySensorList();

		// while drone has moves left and is not finished keep moving
		while (hasMoves() & !finished) {
			var stuck = false;
			String output = null;
			Location newPos;

			if (leftToVisit.size() > 0) {
				// get nearest sensor and calc best direction to get to nearest sensor
				var nearestSensor = getNearestSensor(leftToVisit);
				var sensorPos = ServerParser.wordsToCoords.get(nearestSensor.getLocation());

				int dir = optimalDirection(sensorPos, stuck);
				if (checkStuck() == true) {
					stuck = true;
					// since stuck direction must be bad so add to list
					// find next best direction legal direction
					dir = optimalDirection(start, stuck);
					recentDirs.clear();
				}
				recentDirs.add(dir);
				lastDir = dir;
				output = moves + " " + dir + " " + null;
				newPos = currPos.newPosition(dir);

				if (newPos.inRange(sensorPos)) { //////////moves != 1????
					//sensor in range so take it's reading
					var reading = takeSensorReading(nearestSensor);
					output = moves + " " + dir + " " + nearestSensor.getLocation();

					//create a sensor as a feature and add to the list of Features
					var sensor = DrawMap.drawSensor(nearestSensor, reading);
					feats.add(sensor);
					leftToVisit.remove(nearestSensor);
				}

			} else {// now all stations visited try to return to original starting position
				int dir = optimalDirection(start, stuck);
				if (checkStuck() == true) {
					stuck = true;
					// find next best legal direction that isn't the the direction that
					// makes drone stuck, i.e. badDir
					dir = optimalDirection(start, stuck);
					recentDirs.clear();
				}
				recentDirs.add(dir);
				lastDir = dir;
				output = moves + " " + dir + " " + null;
				newPos = currPos.newPosition(dir);

				if (start.inRange(newPos)) {// maybe change this later to get closer to drone
					finished = true;
				}
			}
			flightInfo.put(currPos, output);
			move(newPos);
		}
		// if out of moves and not all sensors visited color remaining sensors grey
		if (moves == 150 && leftToVisit.size() > 0) {
			for (var sensor : leftToVisit) {
				Feature sens = DrawMap.drawSensor(sensor, -2);
				feats.add(sens);
			}
		}
		// adds all the sensors to the endMap
		DrawMap.setEndMap(FeatureCollection.fromFeatures(feats));
	}

	// method to check if the drone is stuck by counting repeated moves 
	// returns true if drone is stuck
	private boolean checkStuck() {
		int repeat = 0;
		int s = 0;
		
		//this part removes all but the last 4 elements of recentDirs
		if(recentDirs.size() == 0) {
			s = 0;
		}
		else if(recentDirs.size() > 3){
			s = recentDirs.size() - 4;
		}
		for (int j = 0; j < s; j++)
			recentDirs.remove(j);
		
		// checks recent directions taken to see if drone keeps going in
		// zig zagging in opposite directions
		for (int i = 0; i < recentDirs.size() - 1; i++) {
			if (recentDirs.get(i + 1) == oppDir(recentDirs.get(i) - 10)
					|| recentDirs.get(i + 1) == oppDir(recentDirs.get(i) + 10)) {
				repeat++;
			}
		}
		// if 5 series of 'repeated' moves take place, the drone is stuck,
		// return true and clear recentDirs because assume drone will be unstuck
		if (repeat > 2) {
			System.out.println("Stuck at move:" + moves);
			System.out.println(recentDirs + " " + moves);
			//recentDirs.clear();
			return true;
		}
		return false;
	}

	// returns the direction directly opposite the input direction
	private int oppDir(int dir) {
		if (dir >= 0 && dir <= 170) {
			dir = (dir + 180);
		} else if (dir >= 180 && dir <= 350) {
			dir = (dir - 180);
		}
		return dir;
	}

	// method to check current move doesn't go over no fly zones
	// if end position not in a restricted zone and path doesn't cross any polygon
	// then move legal
	public static boolean isLegal(Location startPos, Location endPos) {
		var legal = true;
		// first get list of 1000 points on the line going from the two input parameters
		var coords = Location.pointsOnLine(startPos, endPos);
		// for each point, check if it is inside any of the restricted polygons
		for (var loc : coords) {
			//System.out.println(location.lat + "," + location.lng);
			var pt = Point.fromLngLat(loc.lng, loc.lat);
			for (var polygon : ServerParser.restrZones) {
				//System.out.println(TurfJoins.inside(pt, polygon));
				if (TurfJoins.inside(pt, polygon)) {
					legal = false;
					break;
				}
			}
		}
		return legal;
	}
	
	public static boolean isLegal2(Location startPos, Location endPos) {
		var pointFtrs = new ArrayList<Feature>();
		var polygonFtrs = new ArrayList<Feature>();
		var coords = Location.pointsOnLine(startPos, endPos);
		for (var location : coords) {
			var pt = Point.fromLngLat(location.lng, location.lat);
			var ftr = Feature.fromGeometry(pt);
			pointFtrs.add(ftr);
		}
		for (var polygon : ServerParser.restrZones) {
			var ply = Feature.fromGeometry(polygon);
			polygonFtrs.add(ply);
		}
		var points = FeatureCollection.fromFeatures(pointFtrs);
		var polys = FeatureCollection.fromFeatures(polygonFtrs);

		var fc = TurfJoins.pointsWithinPolygon(points, polys);
		var f = fc.features();
		if (f.size() > 0) {
			return false;
		}
		else {
			return true;
		}
	}
}