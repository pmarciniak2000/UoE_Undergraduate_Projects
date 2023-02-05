package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

public class DrawMap {
	
	//declares the FeatureCollection which will contain all 
	//the sensors and path taken by the drone
	private static FeatureCollection endMap;
		
	//Adds lineString based on the coordinates in path to the FeatureCollection map
	public static FeatureCollection drawPath(List<Location> path, FeatureCollection map) {
		var features = map.features();
		var points = new ArrayList<Point>();
		for (var location : path) {
			points.add(Point.fromLngLat(location.lng, location.lat));
		}
		var myLineString = LineString.fromLngLats(points);
		var myFeature = Feature.fromGeometry(myLineString);
		features.add(myFeature);
		var finalPath = FeatureCollection.fromFeatures(features);
		return finalPath;
	}
	
	//takes in a sensor and it's reading or -1, -2 if the sensor is not visited or has low battery
	//Creates a point with the correct symbol/color at the sensors location and returns it as a feature
	public static Feature drawSensor(Sensor sensor, double reading) {
		var sensorPos = ServerParser.wordsToCoords.get(sensor.getLocation());
		var marker = Point.fromLngLat(sensorPos.lng, sensorPos.lat);
		var ftr = Feature.fromGeometry(marker);
		addProperties(ftr, reading, sensor.getLocation());
		return ftr;
	}

	//Adds properties to the feature passed in according to the value of the reading and location parameters
	private static Feature addProperties(Feature ftr, double reading, String location) {
		ftr.addStringProperty("location", location);
		if (reading >= 0 & reading < 32) {
			ftr.addStringProperty("marker-color", "#00ff00");
			ftr.addStringProperty("rgb-string", "#00ff00");
			ftr.addStringProperty("marker-symbol", "lighthouse");

		}

		else if (reading >= 32 & reading < 64) {
			ftr.addStringProperty("marker-color", "#40ff00");
			ftr.addStringProperty("rgb-string", "#40ff00");
			ftr.addStringProperty("marker-symbol", "lighthouse");

		}

		else if (reading >= 64 & reading < 96) {
			ftr.addStringProperty("marker-color", "#80ff00");
			ftr.addStringProperty("rgb-string", "#80ff00");
			ftr.addStringProperty("marker-symbol", "lighthouse");

		}

		else if (reading >= 96 & reading < 128) {
			ftr.addStringProperty("marker-color", "#c0ff00");
			ftr.addStringProperty("rgb-string", "#c0ff00");
			ftr.addStringProperty("marker-symbol", "lighthouse");

		}

		else if (reading >= 128 & reading < 160) {
			ftr.addStringProperty("marker-color", "#ffc000");
			ftr.addStringProperty("rgb-string", "#ffc000");
			ftr.addStringProperty("marker-symbol", "danger");
		}

		else if (reading >= 160 & reading < 192) {
			ftr.addStringProperty("marker-color", "#ff8000");
			ftr.addStringProperty("rgb-string", "#ff8000");
			ftr.addStringProperty("marker-symbol", "danger");
		}

		else if (reading >= 192 & reading < 224) {
			ftr.addStringProperty("marker-color", "#ff4000");
			ftr.addStringProperty("rgb-string", "#ff4000");
			ftr.addStringProperty("marker-symbol", "danger");
		}

		else if (reading >= 224 & reading < 256) {
			ftr.addStringProperty("marker-color", "#ff0000");
			ftr.addStringProperty("rgb-string", "#ff0000");
			ftr.addStringProperty("marker-symbol", "danger");
		}
		
		else if (reading == -1) {//sensor low battery
			ftr.addStringProperty("marker-color", "#000000");
			ftr.addStringProperty("rgb-string", "#000000");
			ftr.addStringProperty("marker-symbol", "cross");
		}
		else {//sensor not visited
			ftr.addStringProperty("marker-color", "#000000");
			ftr.addStringProperty("rgb-string", "#aaaaaa");
		}
		return ftr;
	}
	
	public static FeatureCollection getEndMap() {
		return endMap;
	}

	public static void setEndMap(FeatureCollection endMap) {
		DrawMap.endMap = endMap;
	}
}
