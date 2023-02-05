package uk.ac.ed.bikerental;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class Booking {

    private static MockDeliveryService mockDeliveryService = new MockDeliveryService();
    
    private static Map<UUID, Booking> allOrderDetails = new HashMap<UUID, Booking>();
      
    private String address, firstName, lastName, postCode, telNo, collectionMethod = "";
    private BigDecimal finalPrice,deposit,priceOfBike;
    private Provider provider;
    private DateRange rentalDuration;
    private ArrayList<Bike> bikesInBooking;
    
   
    public Booking(ArrayList<Bike> bikes, DateRange rentalDuration, Provider provider, BigDecimal priceNoDeposit,boolean isTesting) {

        assert (rentalDuration.toDays() > 0);
        assert (bikes.size() > 0);
        assert (priceNoDeposit.compareTo(BigDecimal.ZERO) > 0);
        
        Scanner scanner = new Scanner(System.in);
        this.priceOfBike = BigDecimal.ZERO;
        this.bikesInBooking = bikes;
        this.rentalDuration = rentalDuration;
        this.deposit = bikesInBooking.get(0).getSimpleDepositAmount();
        if (!provider.getDepreciationMethod().isEmpty()) {
            if (provider.getDepreciationMethod().equals("linear")) {
                this.priceOfBike = Valuation.linearDepreciation(bikes.get(0), LocalDate.now());
            }
            else if (provider.getDepreciationMethod().equals("double declining")) {
                this.priceOfBike = Valuation.doubleDecliningDepreciation(bikes.get(0), LocalDate.now());
            }
            else {this.priceOfBike = priceNoDeposit;}
        } else {this.priceOfBike = priceNoDeposit;}
        this.finalPrice = priceOfBike.add(deposit.multiply(new BigDecimal(bikesInBooking.size())));
        this.provider = provider;
        GenerateQuotes quotes;

        if (!isTesting) {
            System.out.println("First Name: ");
            firstName = scanner.nextLine();
            System.out.println("Last Name: ");
            lastName = scanner.nextLine();
            System.out.println("Post Code: ");
            postCode = scanner.nextLine();
            System.out.println("Address: ");
            address = scanner.nextLine();
            System.out.println("Telephone Number: ");
            telNo = scanner.nextLine();
            while (isValidTel(telNo) == false) {
                System.out.println("Telephone Number must be 11 digits, please re-enter your number: ");
                telNo = scanner.nextLine();
            }
            Location customerLocation = new Location(postCode, address);
            System.out.println("Preferred collection method(Delivery or Pick-up) : ");
            collectionMethod = scanner.nextLine();
            if (isCollectionMethodValid(collectionMethod, customerLocation, provider.getLocation()) == false) {
                System.out.println(
                        "Delivery addresss to far away, press 1 to change collection method to pick-up or any other key to go back to booking: ");
                if (scanner.nextLine() == "1") {
                    collectionMethod = "Pick-up";
                } else {

                    quotes = new GenerateQuotes();
                }

            }

            System.out.println("Do you wish to confirm booking(press enter to confirm): ");
            String input = scanner.nextLine();
            if (input.equals("")) {
                generateConfirmation(this, false);
                updateBikeStatus(rentalDuration, bikesInBooking);
            }

            Order bookingDeliveryOrder = new Order( bikesInBooking, this, provider.getLocation(), customerLocation);
            mockDeliveryService.scheduleDelivery(bookingDeliveryOrder, bookingDeliveryOrder.getDropOffLocation(), bookingDeliveryOrder.getPickUpLocation(), LocalDate.now());

            scanner.close();
        }
        else {
            this.generateDefaultInfo();
        }
    }
    
    public DateRange getRentalDuration() {
        return rentalDuration;
    }

    public String generateDefaultInfo() { //only used for testing
        firstName = "John";
        lastName = "Doe";
        postCode = "EH1 1BG";
        address = "1 East Street";
        telNo = "07815546345";
        collectionMethod = "Pick-up";
        return generateConfirmation(this, true);
    }
    
    private static String generateConfirmation(Booking booking, boolean isTesting) {
       String[] orderDetails = new String[11];
       UUID orderNo;
       String confirmation = "";
       
       if(isTesting = true) {
           orderNo = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
       } 
       else {
           orderNo = generateOrderNo();
       }
       orderDetails[0] = orderNo.toString();
       orderDetails[1] = booking.firstName;
       orderDetails[2] = booking.lastName;
       orderDetails[3] = booking.postCode;
       orderDetails[4] = booking.address;
       orderDetails[5] = booking.telNo;
       orderDetails[6] = booking.collectionMethod;
       orderDetails[7] = "Number of bikes: " + booking.getBikesInBooking().size();
       orderDetails[8] = "Type: " + booking.getBikesInBooking().get(0).getType().getTypeName();
       orderDetails[9] = "£" + (booking.finalPrice).toString() + "(includes deposit of £" + booking.deposit + " per bike)";
       orderDetails[10] = booking.provider.getName();
       allOrderDetails.put(orderNo, booking);
       
       for(String info: orderDetails) {
           confirmation += info;
           confirmation += "\n";
       }
       return confirmation;
    }
    
    private void updateBikeStatus(DateRange rentalDuration, ArrayList<Bike> bikes) {
        for (Bike bike : bikes) {
            bike.reservedDates.add(rentalDuration);
        }
    }

    private static UUID generateOrderNo() {
        UUID orderNo = UUID.randomUUID();
        if(allOrderDetails.containsKey(orderNo)) {
            orderNo = UUID.randomUUID();
        }
        return orderNo;
    }
    
    public boolean isValidTel(String telNo) {
        if (telNo.length() == 11) {
            return true;
        }
        return false;
    }
    
    private boolean isCollectionMethodValid(String collectionMethod, 
            Location customerLocation, Location shopLocation) {
        if (collectionMethod.contentEquals("Delivery")&& customerLocation.isNearTo(shopLocation)) {
            return true;
        }
        return false;
    }
    public Provider getProvider() {
        return provider;
    }

    public static Map<UUID, Booking> getAllOrderDetails() {
        return allOrderDetails;
    }
    public static MockDeliveryService getMockDeliveryService() {
        return mockDeliveryService;
    }

    public ArrayList<Bike> getBikesInBooking() {
        return bikesInBooking;
    }

}
