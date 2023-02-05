package uk.ac.ed.inf.aqmaps;

import java.util.LinkedHashMap;

public class Drone {
	//flightInfo will hold Location of drone at each move and a String with details of each move 
	//LinkedHashMap in order to maintain order
	protected LinkedHashMap<Location,String> flightInfo = new LinkedHashMap<Location, String>();
	protected Location currPos;
	protected int moves = 1;
	protected int lastDir;

	public Drone(Location currPos) {
		this.currPos = currPos;
	}
	
	//moves the drone to the newPos and increments move count by 1 
	protected void move(Location newPos) {
		currPos = newPos;
		moves +=1;
	}
	
	protected boolean hasMoves() {
		return moves < 150; 
	}
	
	protected double takeSensorReading(Sensor sensor) {
		double reading;
		var batt = sensor.getBattery();
		if (batt < 10) {
			reading = -1;// color sensor black with cross since reading void due to battery
		} else {
			reading = Double.parseDouble(sensor.getReading());
		}
		return reading;
	}
}
