package uk.ac.ed.bikerental;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;



public class Bike {
    private LocalDate makeDate;
    private BigDecimal depreciationRate,simpleDepositAmount;
    private BikeType type;
    private String bikeID;
    private boolean inTransit;
    public Provider providerOwner;
    
    public ArrayList<DateRange> reservedDates = new ArrayList<DateRange>();
   
    public Bike(LocalDate makeDate, BikeType type, String bikeID, Provider providerOwner) {
        this.makeDate = makeDate;
        this.providerOwner = providerOwner;
        this.depreciationRate = type.getDepreciationRate();
        this.type = type;
        this.bikeID = bikeID; 
        this.simpleDepositAmount = (BikeType.replacementValues.get(type).multiply(providerOwner.getDepositRate()));
        this.setSimpleDepositAmount(simpleDepositAmount.setScale(0, RoundingMode.HALF_UP));
    }
    
    public Bike(LocalDate makeDate, BikeType type, String bikeID, Provider providerOwner, HashMap<BikeType,BigDecimal> replacementValuesForTesting) {
        this.makeDate = makeDate;
        this.providerOwner = providerOwner;
        this.depreciationRate = type.getDepreciationRate();
        this.type = type;
        this.bikeID = bikeID; 
        this.simpleDepositAmount = replacementValuesForTesting.get(type).multiply(providerOwner.getDepositRate());
        this.setSimpleDepositAmount(simpleDepositAmount.setScale(0, RoundingMode.HALF_UP));
    }

    
    
    public String displayInfo() {
        return "Bike Type: "+ type.getTypeName() + ", built on: " + makeDate.toString() + ", Provided by: "+providerOwner.getName() + " at: "+providerOwner.getShopPostCode();
    }
    
    public BigDecimal getDepreciationRate() {
        return depreciationRate;
    }

    public long getAgeYears(LocalDate currentDate) {
       DateRange bikeRange =  new DateRange(makeDate,currentDate);
       return bikeRange.toYears();
    }
   
    public BikeType getType() {
        return type;
    }
    public String getBikeID() {
        return bikeID;
    }

    public boolean isInTransit() {
        return inTransit;
    }

    public void setInTransit(boolean inTransit) {
        this.inTransit = inTransit;
    }

    public BigDecimal getSimpleDepositAmount() {
        return simpleDepositAmount;
    }

    public void setSimpleDepositAmount(BigDecimal simpleDepositAmount) {
        this.simpleDepositAmount = simpleDepositAmount;
    }
}