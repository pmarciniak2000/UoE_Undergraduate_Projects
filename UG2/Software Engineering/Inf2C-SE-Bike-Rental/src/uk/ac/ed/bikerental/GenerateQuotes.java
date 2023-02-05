package uk.ac.ed.bikerental;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

//Remember to put in cost calculation, base price and simple deposit

public class GenerateQuotes {
    private String bikeType,userPostcode;
    private int numberOfBikes;
    private LocalDate startDate,endDate;
    private DateRange duration;
    private Location userLocation;
    private ArrayList<Provider> listOfLocalProviders = new ArrayList<Provider>();
    private ArrayList<Bike> availableBikes= new ArrayList<Bike>();
    private ArrayList<Bike> bikesToBook = new ArrayList<Bike>();
    private ArrayList<Booking> bookingsMade = new ArrayList<Booking>(); 
   
    public GenerateQuotes() {
        Scanner input = new Scanner(System.in);

        System.out.println("Number of bikes: ");
        this.numberOfBikes = input.nextInt();

        System.out.println("Type of bike: ");
        this.bikeType = input.nextLine();
        
        System.out.println("Your postcode to find local providers: ");
        this.userPostcode = input.nextLine();
        
        
        this.userLocation = new Location(null, userPostcode);
        for (Provider prov : Provider.listOfProviders) {
            if (prov.getLocation().isNearTo(userLocation)) {
                this.listOfLocalProviders.add(prov);
            }
        }
        
        this.duration = calculateDuration(false);
        this.availableBikes = this.makeListOfQuotes();
        if (availableBikes.isEmpty()) {
            System.out.println("No available bikes in this date range, press 1 if you want to extend the period by 3 days before the start and 3 days after the end dates");
            if (input.nextLine().equals("1")) {
                this.duration = calculateDuration(true);
                this.availableBikes = this.makeListOfQuotes();
                if (availableBikes.size()==0) {
                    System.out.println("No available bikes with extension, please resubmit information. Cancelling search"); input.close();return;
                }
            } else {System.out.println("Cancelling search"); input.close();return; }
        }
        
        input.close();
        
        int i = 1;
        for (Bike bike : availableBikes) {
            System.out.println("BIKE NUMBER: "+ i++);
            System.out.println(bike.displayInfo());
            System.out.println("Daily rate: "+bike.providerOwner.typesDailyPrices.get(bike.getType()));
            System.out.println("Deposit amount: "+bike.getSimpleDepositAmount());
            System.out.println();
        }
        
        System.out.println("Please enter the numbers of the bikes you wish to book:");
        String bookingEntry = input.nextLine().replaceAll("\\s+","");
        for (int n = 0; n<bookingEntry.length();n++) {
            bikesToBook.add(availableBikes.get(Character.getNumericValue(bookingEntry.charAt(n))));
        }
        
        for (Provider provider : listOfLocalProviders) {
            ArrayList<Bike> bikesToBookFromProvider = new ArrayList<Bike>();
            for (Bike bike : bikesToBook) {
                if (bike.providerOwner.equals(provider)) {
                    bikesToBookFromProvider.add(bike);
                }
            }
            
            if (!bikesToBookFromProvider.isEmpty()) {
                this.bookingsMade.add(new Booking(bikesToBookFromProvider, duration, provider, calculatePrice(duration,bikesToBookFromProvider),false)) ;
            }
        }
        

    }
    
    public GenerateQuotes(ArrayList<Provider> testListOfLocalProviders,String testBikeTypeName,int testNumberOfBikes, DateRange testDuration, String testPurchaseSequence,HashMap<BikeType,BigDecimal> replacementValues) {

        this.numberOfBikes = testNumberOfBikes;
        this.bikeType = testBikeTypeName;       
        this.userPostcode = "EH1 1BG";
        this.listOfLocalProviders = testListOfLocalProviders;     
        this.duration = testDuration;
        
        this.availableBikes = this.makeListOfQuotes();
        if (availableBikes.isEmpty()) {
            System.out.println("No available bikes in this date range, press 1 if you want to extend the period by 3 days before the start and 3 days after the end dates");
            if (true) {
                this.duration = calculateDuration(true);
                this.availableBikes = this.makeListOfQuotes();
                if (availableBikes.size()==0) {
                    System.out.println("No available bikes with extension, please resubmit information. Cancelling search"); return;
                }
            } else {System.out.println("Cancelling search"); return; }
        }

        
        int i = 1;
        for (Bike bike : availableBikes) {
            System.out.println("BIKE NUMBER: "+ i++);
            System.out.println(bike.displayInfo());
            System.out.println("Daily rate: "+bike.providerOwner.typesDailyPrices.get(bike.getType()));
            System.out.println("Deposit amount: "+bike.getSimpleDepositAmount());
            System.out.println();
        }
        
        System.out.println("Please enter the numbers of the bikes you wish to book:");
        String bookingEntry = testPurchaseSequence;
        for (int n = 0; n<bookingEntry.length();n++) {
            bikesToBook.add(availableBikes.get(Character.getNumericValue(bookingEntry.charAt(n)) - 1));
        }
        System.out.println("Booking " +bikesToBook.size()+" Bike");

        
        for (Provider provider : listOfLocalProviders) {
            ArrayList<Bike> bikesToBookFromProvider = new ArrayList<Bike>();
            for (Bike bike : bikesToBook) {
                if (bike.providerOwner.equals(provider)) {
                    bikesToBookFromProvider.add(bike);
                }
            }
            if (!bikesToBookFromProvider.isEmpty()) {
                System.out.println("Making Booking");
                this.bookingsMade.add(new Booking(bikesToBookFromProvider, duration, provider, calculatePrice(duration,bikesToBookFromProvider,replacementValues),true)) ;
            }
        }
        

    }
    
    
    
    public static BigDecimal calculatePrice(DateRange duration, ArrayList<Bike> bikes) {
        BigDecimal priceNoDeposit = BigDecimal.ZERO;
        BigDecimal rentalDuration = new BigDecimal(duration.toDays());
        for (Bike bike : bikes) {
            BikeType type = bike.getType();
            priceNoDeposit.add(BikeType.replacementValues.get(type));
            priceNoDeposit.add((bike.providerOwner.typesDailyPrices.get(type)).multiply(rentalDuration));
        }
        return priceNoDeposit;
    }
    
    public static BigDecimal calculatePrice(DateRange duration, ArrayList<Bike> bikes, int depreciationChoice) {
        BigDecimal priceNoDeposit = BigDecimal.ZERO;
        BigDecimal rentalDuration = new BigDecimal(duration.toDays());
        switch (depreciationChoice) {
            case 1: 
                for (Bike bike : bikes) {
                    BikeType type = bike.getType();
                    priceNoDeposit.add(BikeType.replacementValues.get(type));
                    priceNoDeposit.add((Valuation.linearDepreciation(bike, LocalDate.now())).multiply(rentalDuration));
                }
                break;
            case 2:
                for (Bike bike : bikes) {
                    BikeType type = bike.getType();
                    priceNoDeposit.add(BikeType.replacementValues.get(type));
                    priceNoDeposit.add((Valuation.linearDepreciation(bike, LocalDate.now())).multiply(rentalDuration));
                }
                break;
            default:
                System.out.println("Not a valid choice, use parameter 1 for linear depreciation and 2 for double declining depreciation.");
                return BigDecimal.ZERO;
          
        }
        return priceNoDeposit;
    }
    
    public static BigDecimal calculatePrice(DateRange duration, ArrayList<Bike> bikes, HashMap<BikeType,BigDecimal> replacementValues) {
        BigDecimal priceNoDeposit = BigDecimal.ZERO;
        BigDecimal rentalDuration = new BigDecimal(duration.toDays());
        for (Bike bike : bikes) {
            BikeType type = bike.getType();
            BigDecimal dailyPriceOfBike = bike.providerOwner.typesDailyPrices.get(type);
            priceNoDeposit = priceNoDeposit.add(dailyPriceOfBike);
        }
        return priceNoDeposit.multiply(rentalDuration);
    }
    
    private DateRange calculateDuration(boolean extension) {
        Scanner dateInput = new Scanner(System.in);
        
        int startYear,startMonth,startDay;
        String[] startInput;
        System.out.println("Start date of renting in day/month/year: ");
        startInput = dateInput.nextLine().split("/");
        startDay = Integer.parseInt(startInput[0]);
        startMonth = Integer.parseInt(startInput[1]);
        startYear = Integer.parseInt(startInput[2]);
        
        int endYear,endMonth,endDay;
        String[] endInput;
        System.out.println("End date of renting in day/month/year: ");
        endInput = dateInput.nextLine().split("/");
        endDay = Integer.parseInt(endInput[0]);
        endMonth = Integer.parseInt(endInput[1]);
        endYear = Integer.parseInt(endInput[2]);
              
        this.startDate = LocalDate.of(startYear,startMonth,startDay);
        this.endDate = LocalDate.of(endYear,endMonth,endDay);
        
        if (extension) {
            startDate.minusDays(3);
            endDate.plusDays(3);
        }
        
        if (startDate.isAfter(endDate)) {
            System.out.println("Invalid date range");
        }
        
        this.duration = new DateRange(startDate,endDate);
        dateInput.close();
        return duration;
    }
    
    
    public ArrayList<Bike> makeListOfQuotes() {
        ArrayList<Bike> returnedBikes = new ArrayList<Bike>();
        
        for (Provider provider : listOfLocalProviders) {
            for (Bike bike : provider.stock.values()) {
                if (bike.getType().getTypeName().equals(this.bikeType)) {
                    boolean availableOnDates = true;
                    for (DateRange reservedDate : bike.reservedDates) {
                        if (duration.overlaps(reservedDate)) {
                            availableOnDates = false;
                            break;
                        }
                    }
                    if (availableOnDates) {
                        returnedBikes.add(bike);
                    }
                }
            }
        }
        
        if (returnedBikes.size() < numberOfBikes) {
            System.out.println(returnedBikes.size());
            System.out.println("Could not find enough available bikes.");
        }
        return returnedBikes;

    }
    public ArrayList<Booking> getBookingsMade() {
        return bookingsMade;
    }

    
}
