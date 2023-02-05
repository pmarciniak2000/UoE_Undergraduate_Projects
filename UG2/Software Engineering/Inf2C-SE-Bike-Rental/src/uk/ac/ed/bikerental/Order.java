package uk.ac.ed.bikerental;

import java.util.ArrayList;

public class Order implements Deliverable {

    private ArrayList<Bike> bikesInOrder;
    private Booking bookingWithOrder;
    private Location pickUpLocation,dropOffLocation;
    
    public Order(ArrayList<Bike> bikes, Booking booking, Location pickUpLocation, Location dropOffLocation) {
        this.bikesInOrder = bikes;
        this.bookingWithOrder = booking;
        this.setPickUpLocation(pickUpLocation);
        this.setDropOffLocation(dropOffLocation);
    }
    
    @Override
    public void onPickup() {
        for (Bike bike : bikesInOrder ) {
            bike.setInTransit(true);
        }
    }

    @Override
    public void onDropoff() {
        for (Bike bike : bikesInOrder ) {
            bike.setInTransit(false);  
        }
    }

    public Location getPickUpLocation() {
        return pickUpLocation;
    }

    public void setPickUpLocation(Location pickUpLocation) {
        this.pickUpLocation = pickUpLocation;
    }

    public Location getDropOffLocation() {
        return dropOffLocation;
    }

    public void setDropOffLocation(Location dropOffLocation) {
        this.dropOffLocation = dropOffLocation;
    }

}
