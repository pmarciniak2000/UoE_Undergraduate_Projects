package uk.ac.ed.bikerental;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Provider {
    public static ArrayList<Provider> listOfProviders = new ArrayList<Provider>();
    
    private String shopAddress,shopPostCode,name,telNo;
    private ArrayList<Provider> partneredProviders;
    private BigDecimal depositRate;
    private Location location;
    private String depreciationMethod;
    

   
    public Map<String, Bike> stock = new HashMap<String, Bike>();
    public Map<BikeType, BigDecimal> typesDailyPrices = new HashMap<BikeType, BigDecimal>();
    
    public Provider(String name, String shopAddress, String shopPostCode, BigDecimal depositRate) {
        this.name = name;
        this.shopAddress = shopAddress;
        this.shopPostCode = shopPostCode;
        this.depositRate = depositRate;
        this.location = new Location(shopPostCode, shopAddress);
        this.partneredProviders = new ArrayList<Provider>();
        this.depreciationMethod = "";
    }
    
    public Provider(String name, String shopAddress, String shopPostCode, BigDecimal depositRate,String depreciationMethod) {
        this.name = name;
        this.shopAddress = shopAddress;
        this.shopPostCode = shopPostCode;
        this.depositRate = depositRate;
        this.location = new Location(shopPostCode, shopAddress);
        this.partneredProviders = new ArrayList<Provider>();
        this.depreciationMethod = depreciationMethod;
    }
    
    public void addBikeTypePrice(BikeType bikeType, BigDecimal price) {
        this.typesDailyPrices.put(bikeType, price);
    }
    
    public void addBikeToStock(LocalDate makeDate, BikeType type, String bikeID) {
        
        if (BikeType.replacementValues.containsKey(type)) {
            Bike bike = new Bike(makeDate, type, bikeID,this);
            stock.put(bikeID, bike);
        }
        else {
            System.out.println("This bike type does not exist");
        }
    }
    
    public void addBikeToStock(Bike bike) {
        stock.put(bike.getBikeID(), bike);
    }    
    
    public boolean customerReturnsBikes(ArrayList<Bike> bikesToReturn, UUID bookingNumber) {
        Booking bookingForBikes = null;
        try {bookingForBikes = Booking.getAllOrderDetails().get(bookingNumber);}
        catch (Exception nullPointerException) {System.out.println("No booking found for booking number, aborting."); return false;}
        
        Provider providerOfBooking = bookingForBikes.getProvider();
        
        if (!partneredProviders.contains(providerOfBooking)) {
            if (bookingForBikes.getProvider().equals(bikesToReturn.get(0).providerOwner) ) {
                System.out.println("Bike(s) returned to original provider!");
                return removeReservedDates(bikesToReturn,bookingForBikes);
            } else {System.out.println("Bike(s) belong to unpartnered provider, order must be returned to their original owner or a provider partnered with them.");return false;}
        } else {
            if (removeReservedDates(bikesToReturn,bookingForBikes)) {
                Order bikeToPartner = new Order(bikesToReturn, bookingForBikes, this.location, providerOfBooking.getLocation());
                Booking.getMockDeliveryService().scheduleDelivery(bikeToPartner, bikeToPartner.getPickUpLocation(), bikeToPartner.getDropOffLocation(), LocalDate.now());
                System.out.println("Bike(s) returned to partnered provider!");
                return true;
            } else {return false;}
            
        }
    }
    
    public boolean customerReturnsBikes(ArrayList<Bike> testBikesToReturn, UUID testBookingNumber, DeliveryService testDeliveryService, HashMap<UUID,Booking> testAllOrderDetails) {
        Booking bookingForBikes = null;
        bookingForBikes = testAllOrderDetails.get(testBookingNumber);
        if (bookingForBikes==null) {System.out.println("No booking found for booking number, aborting."); return false;}
        
        Provider providerOfBooking = bookingForBikes.getProvider();
        
        if (!partneredProviders.contains(providerOfBooking)) {
            if (bookingForBikes.getProvider().equals(testBikesToReturn.get(0).providerOwner) ) {
                System.out.println("Bike(s) returned to original provider!");
                return removeReservedDates(testBikesToReturn,bookingForBikes);
            } else {System.out.println("Bike(s) belong to unpartnered provider, order must be returned to their original owner or a provider partnered with them.");return false;}
        } else {
            if (removeReservedDates(testBikesToReturn,bookingForBikes)) {
                Order bikeToPartner = new Order(testBikesToReturn, bookingForBikes, this.location, providerOfBooking.getLocation());
                testDeliveryService.scheduleDelivery(bikeToPartner, bikeToPartner.getPickUpLocation(), bikeToPartner.getDropOffLocation(), LocalDate.now());
                System.out.println("Bike(s) returned to partnered provider!");
                return true;
            } else {return false;}
            
        }
    }
    
    private boolean removeReservedDates(ArrayList<Bike> bikes, Booking booking) {
        try {
            for (Bike bike : bikes) {
                bike.reservedDates.remove(booking.getRentalDuration());
            }
        } catch (Exception nullPointerException) {
            System.out.println("Error in removing bike reserved dates ( date not found )");
            return false;
        }   
        return true;
    }
    
    public String getShopAddress() {
        return shopAddress;
    }

    public String getShopPostCode() {
        return shopPostCode;
    }

    public void setDepositRate(BigDecimal NewDepositRate) {
        depositRate = NewDepositRate;
    }
    
    public BigDecimal getDepositRate() {
        return depositRate;
    }

    public void showStock() {
        for(String key: stock.keySet()) {
            System.out.println(key);
            System.out.println(stock.get(key));
        }
        
    }
    public String getDepreciationMethod() {
        return depreciationMethod;
    }

    public void setDepreciationMethod(String depreciationMethod) {
        this.depreciationMethod = depreciationMethod;
    }

    
    public String getName() {
        return name;
    }

    public ArrayList<Provider> getPartneredProviders() {
        return partneredProviders;
    }

    public void addPartneredProvider(Provider partneredProvider) {
        this.partneredProviders.add(partneredProvider);
    }
    
    public void addPartneredProvider(String partneredProviderName) {
        for (Provider prov : listOfProviders) {
            if (prov.getName().equals(partneredProviderName)) {
                this.partneredProviders.add(prov);
                return;
            }
        }
    }
    
    public Location getLocation() {
        return location;
    }
}
