package uk.ac.ed.inf.aqmaps;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;

public class App {
	
	// writes a file given filename and string
	private static void writeToFile(String fileName, String str) throws IOException {
		var writer = new FileWriter(fileName);
		writer.write(str);
		writer.close();
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		// read 7 inputs from command line:
		// 1- Day DD
		// 2- Month MM
		// 3- Year YYYY
		// 4- Drone starting lat ~55
		// 5- Drone starting long ~-3
		// 6- Random Seed (5678)
		// 7- Connecting Port

		// Check if the input arguments are enough
		if (args.length != 7) {
			System.out.println("Incorrect number of input arguments!");
			System.exit(0);
		}
		// Read the input arguments
		String day = args[0];
		int day_int = Integer.parseInt(day);
		String month = args[1];
		int month_int = Integer.parseInt(month);

		String year = args[2];

		// Check if the input month is valid
		if (month_int > 12 || month_int < 1) {
			System.out.println("Invalid month input!");
			System.exit(0);
		}

		// Check if the input day is valid
		if (day_int > 31 || day_int < 1) {
			System.out.println("Invalid day input!");
			System.exit(0);
		}

		var latitude = Double.parseDouble(args[3]);
		var longitude = Double.parseDouble(args[4]);
		//int seed = Integer.parseInt(args[5]);
		int port = Integer.parseInt(args[6]);

		// Generate the random seed, prob not needed
		//Random rnd = new Random();
		//rnd.setSeed(seed);
		
		// Generate the map GeoJSON URI for date given as arguments
		var mapURI = "http://localhost:" + port + "/" + "maps" + "/" + year + "/" + month + "/" + day + "/" + "air-quality-data.json";
		
		//parse servers from string into a list sensors
		ServerParser.parseSensors(mapURI);
		//get all the restricted fly zones into polygon list
		ServerParser.parseNoFlyZones(port);
		//get all coordinate address for the station
		ServerParser.wordsToCoords(port);
		
		var drone = new DroneMoves(new Location(latitude, longitude));

		//calls method to populate Map containing flight path and details of each move
		drone.flight();
		var flightCoords = new ArrayList<Location>(drone.flightInfo.keySet());
		var flightDetails = new ArrayList<String>(drone.flightInfo.values());
		//System.out.println(details.size());
		//System.out.println(path.size());

		//Create final output string using details from flightPath
		var output = formatOutputString(flightCoords, flightDetails);
			
		//add the final path to the map
		DrawMap.setEndMap(DrawMap.drawPath(flightCoords, DrawMap.getEndMap()));
		
		//Generate output filenames
		var fileNameTxt = "flightpath-" + day + "-" + month + "-" + year + ".txt";
		var fileNameJson = "readings-" + day + "-" + month + "-" + year + ".geojson";
		
		//write output string to txt file and endMap to a geojson file
		writeToFile(fileNameTxt, output);
		writeToFile(fileNameJson, DrawMap.getEndMap().toJson());
		
		
		//POINTS ON LINE TEST
		Location pos1 = new Location(55.94549935134597,-3.1875485108651738);
		Location pos2 = new Location(55.94549935134597,-3.1872485108651736);
		var ftes = new ArrayList<Feature>();
		
		var pLine = Location.pointsOnLine(pos1, pos2);
		for (Location l: pLine) {
			Point p = Point.fromLngLat(l.lng, l.lat);
			var fp = Feature.fromGeometry(p);
			fp.addStringProperty("coords", l.lng + "," + l.lat);
			ftes.add(fp);
		}
		var t1 = FeatureCollection.fromFeatures(ftes);
		//System.out.print(ftes.size());
		writeToFile("lineTst.geojson", t1.toJson());
		
		//System.out.println(DroneMoves.isLegal(pos2,pos1));

		
		/*
		 * var polygonFtrs = new ArrayList<Feature>(); for (var polygon :
		 * ServerParser.restrZones) { var ply = Feature.fromGeometry(polygon);
		 * polygonFtrs.add(ply); } var fc = FeatureCollection.fromFeatures(polygonFtrs);
		 * writeToFile("own-no-fly-zones.geojson", fc.toJson());
		 */
	}
	
	//Concatenate and formats the details and the path to form the final output string
	private static String formatOutputString(ArrayList<Location> path, ArrayList<String> details) {
		var finalString = new ArrayList<String>();
		for (int i = 0; i < path.size() - 1; i++) { 
			var dets = details.get(i).split(" ");
			//0 = move, 1 = angle, 2 = station location
			finalString.add(dets[0] + "," + path.get(i).lng + "," + path.get(i).lat + "," + dets[1]  + "," +
					path.get(i+1).lng + "," + path.get(i+1).lat + "," + dets[2]);
		}
		
		var output = "";
		for (String info : finalString) {
			output += info + "\n";
		}
		return output;
	}
}
