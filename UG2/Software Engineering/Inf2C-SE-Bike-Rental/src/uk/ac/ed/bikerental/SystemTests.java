package uk.ac.ed.bikerental;

import org.junit.jupiter.api.*;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class SystemTests {

    String defaultBookingInfo1,defaultBookingInfo2,defaultBookingInfo3,defaultBookingInfoMore3,defaultUUID;
    BikeType type1,type2,type3;   
    HashMap<BikeType,BigDecimal> replacementValues;
    Bike bike1,bike2,bike3,bike33,bike333;
    Provider provider1,provider2,provider3;
    BigDecimal depositRate1,depositRate2,depositRate3,priceNoDeposit1,priceNoDeposit2,priceNoDeposit3;
    Booking booking1,booking2,booking3;
    ArrayList<Bike> listOfBikes1,listOfBikes2,listOfBikes3,listOfBikesMore3;
    ArrayList<Provider> listOfProvidersAll;
    DateRange rentalDuration1,rentalDuration2,rentalDuration3;
    DeliveryService mockDeliveryService;
    HashMap<UUID,Booking> allOrderDetails;
    
    @BeforeEach
    void setUp() throws Exception {  
        this.defaultUUID = "38400000-8cf0-11bd-b23e-10b96e4ef00d";
        
        DeliveryServiceFactory.setupMockDeliveryService();
        this.mockDeliveryService = DeliveryServiceFactory.getDeliveryService();
        
        this.rentalDuration1 = new DateRange(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 2)); // One day
        this.rentalDuration2 = new DateRange(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 3)); // Two days
        this.rentalDuration3 = new DateRange(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 4)); // Three days
        
        this.type1 = new BikeType(new BigDecimal(0.1f), "type1");
        this.type2 = new BikeType(new BigDecimal(0.2f), "type2");
        this.type3 = new BikeType(new BigDecimal(0.3f), "type3");
        
        this.replacementValues = new HashMap<BikeType,BigDecimal>();
        replacementValues.put(type1, new BigDecimal(1000));
        replacementValues.put(type2, new BigDecimal(2000));
        replacementValues.put(type3, new BigDecimal(3000));
        
        this.depositRate1 = new BigDecimal(0.1f);
        this.depositRate2 = new BigDecimal(0.2f);
        this.depositRate3 = new BigDecimal(0.3f);       
        
        this.provider1 = new Provider("Edi Bikes One", "1 West Street", "EH1 1HE", depositRate1);
        this.provider2 = new Provider("Edi Bikes Two", "2 West Street", "EH2 2HE", depositRate2);
        this.provider3 = new Provider("Edi Bikes Three", "3 West Street", "EH3 3HE", depositRate3);  
        provider2.addPartneredProvider(provider1);
        
        this.bike1 = new Bike(LocalDate.now().minusYears(1), type1, "1",provider1,replacementValues);        
        this.bike2 = new Bike(LocalDate.now().minusYears(2), type2, "2",provider2,replacementValues);
        this.bike3 = new Bike(LocalDate.now().minusYears(3), type3, "3",provider3,replacementValues);
        this.bike33 = new Bike(LocalDate.now().minusYears(3), type3, "33",provider3,replacementValues);
        this.bike333 = new Bike(LocalDate.now().minusYears(3), type3, "333",provider3,replacementValues);
        provider1.addBikeToStock(bike1);provider2.addBikeToStock(bike2);provider3.addBikeToStock(bike3);
        provider3.addBikeToStock(bike33);provider3.addBikeToStock(bike333);
       
        this.listOfBikes1 = new ArrayList<Bike>();
        listOfBikes1.add(bike1);
        this.listOfBikes2 = new ArrayList<Bike>();
        listOfBikes2.add(bike2);
        this.listOfBikes3 = new ArrayList<Bike>();
        listOfBikes3.add(bike3);
        this.listOfBikesMore3 = new ArrayList<Bike>();
        listOfBikesMore3.add(bike3);
        listOfBikesMore3.add(bike33);
        listOfBikesMore3.add(bike333);
        
        provider1.addBikeTypePrice(type1, new BigDecimal(10));
        provider2.addBikeTypePrice(type2, new BigDecimal(20));
        provider3.addBikeTypePrice(type3, new BigDecimal(30));
        this.priceNoDeposit1 = GenerateQuotes.calculatePrice(rentalDuration1, listOfBikes1,replacementValues);
        this.priceNoDeposit2 = GenerateQuotes.calculatePrice(rentalDuration2, listOfBikes2,replacementValues);
        this.priceNoDeposit3 = GenerateQuotes.calculatePrice(rentalDuration3, listOfBikesMore3,replacementValues);
 
        this.listOfProvidersAll = new ArrayList<Provider>();
        listOfProvidersAll.add(provider1);
        listOfProvidersAll.add(provider2);
        listOfProvidersAll.add(provider3);
        
        this.booking1 = new Booking(listOfBikes1, rentalDuration1, provider1, priceNoDeposit1, true);
        this.booking2 = new Booking(listOfBikes2, rentalDuration2, provider2, priceNoDeposit2, true);
        this.booking3 = new Booking(listOfBikesMore3, rentalDuration3, provider3, priceNoDeposit3, true);

        defaultBookingInfo1 = "38400000-8cf0-11bd-b23e-10b96e4ef00d\nJohn\nDoe\nEH1 1BG\n1 East Street\n07815546345\nPick-up\nNumber of bikes: 1\nType: type1\n£110(includes deposit of £100 per bike)\nEdi Bikes One\n";
        defaultBookingInfo2 = "38400000-8cf0-11bd-b23e-10b96e4ef00d\nJohn\nDoe\nEH1 1BG\n1 East Street\n07815546345\nPick-up\nNumber of bikes: 1\nType: type2\n£440(includes deposit of £400 per bike)\nEdi Bikes Two\n";
        defaultBookingInfo3 = "38400000-8cf0-11bd-b23e-10b96e4ef00d\nJohn\nDoe\nEH1 1BG\n1 East Street\n07815546345\nPick-up\nNumber of bikes: 1\nType: type3\n£990(includes deposit of £900 per bike)\nEdi Bikes Three\n";
        defaultBookingInfoMore3 = "38400000-8cf0-11bd-b23e-10b96e4ef00d\nJohn\nDoe\nEH1 1BG\n1 East Street\n07815546345\nPick-up\nNumber of bikes: 3\nType: type3\n£2970(includes deposit of £900 per bike)\nEdi Bikes Three\n";
        
        this.allOrderDetails = new HashMap<UUID,Booking>();
        
    }

    @Test
    void testBookingConfirmationOutput() {
        // Tests that bookings made return the expected information.
        
        assertThat(booking1.generateDefaultInfo(), is(defaultBookingInfo1));        
        assertThat(booking2.generateDefaultInfo(), is(not(defaultBookingInfo1)));      
        assertThat(booking3.generateDefaultInfo(), is(defaultBookingInfoMore3));
    }
    
    @Test
    void testGenerateQuotesUseCase() {
        // Tests that quotes made and bookings made through those quotes return the expected information, including booking multiple bikes.
        
        GenerateQuotes testQuote1 = new GenerateQuotes(listOfProvidersAll,"type1",1, rentalDuration1, "1",replacementValues);
        assertThat(testQuote1.getBookingsMade().get(0).generateDefaultInfo(), is(defaultBookingInfo1));
        
        GenerateQuotes testQuote2 = new GenerateQuotes(listOfProvidersAll,"type2",1, rentalDuration2, "1",replacementValues);
        assertThat(testQuote2.getBookingsMade().get(0).generateDefaultInfo(), is(not(defaultBookingInfo1)));
        
        GenerateQuotes testQuote3 = new GenerateQuotes(listOfProvidersAll,"type3",1, rentalDuration3, "123",replacementValues);
        assertThat(testQuote3.getBookingsMade().get(0).generateDefaultInfo(), is(defaultBookingInfoMore3));

    }
    
    @Test
    void testReturnBikes() {
        // Tests that bikes returned to providers meet expected behaviour i.e. dates removed, delivery scheduled, delivery to partner if needed, false if not partnered or no order ever made.
        
        GenerateQuotes testQuote1 = new GenerateQuotes(listOfProvidersAll,"type1",1, rentalDuration1, "1",replacementValues);
        GenerateQuotes testQuote3 = new GenerateQuotes(listOfProvidersAll,"type3",1, rentalDuration3, "1",replacementValues);
        System.out.println();
        
        allOrderDetails.put(UUID.fromString(defaultUUID),testQuote1.getBookingsMade().get(0));
        assertTrue(provider1.customerReturnsBikes(listOfBikes1, UUID.fromString(defaultUUID),mockDeliveryService,allOrderDetails));
        allOrderDetails.remove(UUID.fromString(defaultUUID));
        
        allOrderDetails.put(UUID.fromString(defaultUUID),testQuote1.getBookingsMade().get(0));
        assertTrue(provider2.customerReturnsBikes(listOfBikes1, UUID.fromString(defaultUUID),mockDeliveryService,allOrderDetails));
        allOrderDetails.remove(UUID.fromString(defaultUUID));

        assertFalse(provider3.customerReturnsBikes(listOfBikes3, UUID.fromString(defaultUUID),mockDeliveryService,allOrderDetails));
        
        allOrderDetails.put(UUID.fromString(defaultUUID),testQuote3.getBookingsMade().get(0));
        assertFalse(provider3.customerReturnsBikes(listOfBikes2, UUID.fromString(defaultUUID),mockDeliveryService,allOrderDetails));
        allOrderDetails.remove(UUID.fromString(defaultUUID));

    }
}
