package uk.ac.ed.inf.heatmap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.GeoJson;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.*;

public class App {
	// list to hold all readings from predictions.txt
	private static List<Integer> readings = new ArrayList<Integer>();

	// declare corners of 'play' area, final since 'play' area is fixed
	static final Location topL = new Location(55.946233, -3.192473);
	static final Location topR = new Location(55.946233, -3.184319);
	static final Location botL = new Location(55.942617, -3.192473);
	static final Location botR = new Location(55.942617, -3.184319);

	// writes a file given filename and string
	private static void writeToFile(String fileName, String str) throws IOException {
		var writer = new FileWriter(fileName);
		writer.write(str);
		writer.close();
	}

	// reads text file and parses its values into the 'readings' list
	private static void parseReadings(List<Integer> readings, String inputFile) throws IOException {

		try {
			var preds = new BufferedReader(new FileReader(inputFile));
			String str;

			while ((str = preds.readLine()) != null) {
				String[] values = str.split("[\\s,]+");
				for (String val : values) {
					readings.add(Integer.parseInt(val));
				}
			}
			preds.close();
		} catch (IOException e) {
			System.out.println("File Read Error");
		}
	}

	// generates a geojson heatmap based on given list of readings
	private static String drawHeatmap(List<Integer> readings) {
		var polygons = new ArrayList<Feature>();

		// finds dimensions of play area and each polygon
		var latDist = Math.abs(topL.latitude - botL.latitude);
		var longDist = Math.abs(topL.longitude - topR.longitude);
		var offsetLat = latDist / 10;
		var offsetLng = longDist / 10;

		// keeps track of which reading/polygon is being generated
		int position = 0;

		for (int row = 1; row <= 10; row++) {
			for (int col = 1; col <= 10; col++) {
				var shape = new ArrayList<Point>();

				// finds coordinates of each polygons points
				shape.add(Point.fromLngLat(topL.longitude + offsetLng * (col - 1),
						topL.latitude - offsetLat * (row - 1)));
				shape.add(Point.fromLngLat(topL.longitude + offsetLng * col,
						topL.latitude - offsetLat * (row - 1)));
				shape.add(Point.fromLngLat(topL.longitude + offsetLng * col,
						topL.latitude - offsetLat * row));
				shape.add(Point.fromLngLat(topL.longitude + offsetLng * (col - 1),
						topL.latitude - offsetLat * row));

				var polygon = Polygon.fromLngLats(List.of(shape));
				var ftr = Feature.fromGeometry(polygon);
				int reading = readings.get(position);

				if (reading >= 0 & reading < 32) {
					ftr.addStringProperty("fill", "#00ff00");
					ftr.addStringProperty("rgb-string", "#00ff00");
				}

				else if (reading >= 32 & reading < 64) {
					ftr.addStringProperty("fill", "#40ff00");
					ftr.addStringProperty("rgb-string", "#40ff00");
				}

				else if (reading >= 64 & reading < 96) {
					ftr.addStringProperty("fill", "#80ff00");
					ftr.addStringProperty("rgb-string", "#80ff00");
				}

				else if (reading >= 96 & reading < 128) {
					ftr.addStringProperty("fill", "#c0ff00");
					ftr.addStringProperty("rgb-string", "#c0ff00");
				}

				else if (reading >= 128 & reading < 160) {
					ftr.addStringProperty("fill", "#ffc000");
					ftr.addStringProperty("rgb-string", "#ffc000");
				}

				else if (reading >= 160 & reading < 192) {
					ftr.addStringProperty("fill", "#ff8000");
					ftr.addStringProperty("rgb-string", "#ff8000");
				}

				else if (reading >= 192 & reading < 224) {
					ftr.addStringProperty("fill", "#ff4000");
					ftr.addStringProperty("rgb-string", "#ff4000");
				}

				else if (reading >= 224 & reading < 256) {
					ftr.addStringProperty("fill", "#ff0000");
					ftr.addStringProperty("rgb-string", "#ff0000");
				}

				ftr.addNumberProperty("fill-opacity", 0.75);
				polygons.add(ftr);
				position++;
			}
		}

		var map = FeatureCollection.fromFeatures(polygons);
		var heatmap = map.toJson();
		return heatmap;
	}

	public static void main(String[] args) throws IOException {
		String inputFile = args[0];
		parseReadings(readings, inputFile);
		writeToFile("heatmap.geojson", drawHeatmap(readings));
	}
}
