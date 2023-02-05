package uk.ac.ed.inf.aqmaps;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Polygon;

public class ServerParser {
	
	public static final HashMap<String, Location> wordsToCoords = new HashMap<String,Location>();
	public static final List<Polygon> restrZones = new ArrayList<Polygon>();
	private static List<Sensor> sensors = new ArrayList<Sensor>();

	//reads data of server at given URI and returns it as a string
	public static String readServer(String uri) throws IOException, InterruptedException {
		var client = HttpClient.newHttpClient();
		var request = HttpRequest.newBuilder().uri(URI.create(uri))
				.build();
		//HttpResponse<String> response
		var response = client.send(request, BodyHandlers.ofString());
		String str = response.body();
		return str;
	}
	
	//populates the HashMap linking all the what3words addresses to their coordinate locations for the given day
	public static void wordsToCoords(int port) throws IOException, InterruptedException {
		for (var sensor : sensors) {
			var what3 = sensor.getLocation();
			//split the what3 string into list of 3 individual strings, then generate URI with them
			var words = what3.split("\\.");
			var coords = readServer("http://localhost:" + port + "/" + "words/" + words[0]
					+ "/" + words[1] + "/" + words[2] + "/" + "details.json");
			//reference StackOverflow post!!!
			JsonObject root = new Gson().fromJson(coords, JsonObject.class);
			Location pos = new Gson().fromJson(root.get("coordinates"), Location.class);
			wordsToCoords.put(what3, pos);
		}
	}
	
	//reads the no fly zones and returns a list of the restricted zones as a polygon list
	//or just pass in port from main class
	public static void parseNoFlyZones(int port) throws IOException, InterruptedException{
		var noFly = readServer("http://localhost:" + port + "/" + "buildings" + "/" + "no-fly-zones.geojson");
		var collection = FeatureCollection.fromJson(noFly);
		var features = collection.features();
		for (var feature : features) {
			var zone = ((Polygon) feature.geometry());
			restrZones.add(zone);
		}
		//System.out.print(restrZones.size());
	}
	
	//parses all the sensors for given day into a list of sensors
	public static void parseSensors(String uri) throws IOException, InterruptedException{
		var sensorString = readServer(uri);
		Type listType = new TypeToken<ArrayList<Sensor>>() {}.getType();
		sensors = new Gson().fromJson(sensorString, listType);
	}
	
	public static List<Sensor> getSensors() {
		return sensors;
	}
}
