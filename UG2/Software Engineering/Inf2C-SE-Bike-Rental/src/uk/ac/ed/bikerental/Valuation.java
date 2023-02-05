package uk.ac.ed.bikerental;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Valuation implements ValuationPolicy{

    @Override
    public BigDecimal calculateValue(Bike bike, LocalDate date) {
        Scanner input = new Scanner(System.in);
        System.out.println("Bike value calculation, please choose depreciation type: 1) Linear Depreciation or 2) Double Declining Depreciation, type the corresponding letter");
        if (input.nextLine().equals("1")) {
            input.close();
            linearDepreciation(bike,date);
        }
        else if (input.nextLine().equals("2")) {
            input.close();
            doubleDecliningDepreciation(bike,date);
        }
        else {input.close(); System.out.println("Invalid Input.");} 
        return null;
    }
    
    public BigDecimal calculateValue(Bike bike, LocalDate date, int choice) {
        switch (choice) {
            case 1: 
                return linearDepreciation(bike,date);
            case 2:
                return doubleDecliningDepreciation(bike,date);
            default:
                System.out.println("Not a valid choice, use parameter 1 for linear depreciation and 2 for double declining depreciation.");
                return BigDecimal.ZERO;
        }
    }

    public static BigDecimal linearDepreciation(Bike bike, LocalDate date) {
        try {
            BigDecimal replacementCost = bike.getType().getReplacementValue();
            BigDecimal bikeAge = new BigDecimal(bike.getAgeYears(date));
            
            return replacementCost.subtract(replacementCost.multiply(bike.getDepreciationRate()).multiply(bikeAge));
        } catch (Exception NullPointerException) { System.out.println("Bike provider must set bike depreciation rate first."); }
        
        return null;
    }
    
    public static BigDecimal linearDepreciation(Bike bike, LocalDate date, HashMap<BikeType,BigDecimal> replacementCosts) {
        try {
            BigDecimal replacementCost = replacementCosts.get(bike.getType());
            BigDecimal bikeAge = new BigDecimal(bike.getAgeYears(date));
            
            return replacementCost.subtract(replacementCost.multiply(bike.getDepreciationRate()).multiply(bikeAge));
        } catch (Exception NullPointerException) { System.out.println("Bike provider must set bike depreciation rate first."); }
        
        return null;
    }
    
    public static BigDecimal doubleDecliningDepreciation(Bike bike, LocalDate date) {
        try {
            BigDecimal replacementCost = bike.getType().getReplacementValue();
            int bikeAge = (int) bike.getAgeYears(date);
            BigDecimal two= new BigDecimal(2);
            
            return replacementCost.multiply((BigDecimal.ONE.subtract((two).multiply(bike.getDepreciationRate()))).pow(bikeAge));
        } catch (Exception NullPointerException) { System.out.println("Bike provider must set bike depreciation rate first."); }
        
        return null;
        
    }
    
    public static BigDecimal doubleDecliningDepreciation(Bike bike, LocalDate date,HashMap<BikeType,BigDecimal> replacementCosts) {
        try {
            BigDecimal replacementCost = replacementCosts.get(bike.getType());
            int bikeAge = (int) bike.getAgeYears(date);
            BigDecimal two= new BigDecimal(2);
            
            return replacementCost.multiply((BigDecimal.ONE.subtract((two).multiply(bike.getDepreciationRate()))).pow(bikeAge));
        } catch (Exception NullPointerException) { System.out.println("Bike provider must set bike depreciation rate first."); }
        
        return null;
        
    }
}
