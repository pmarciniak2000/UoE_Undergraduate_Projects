package uk.ac.ed.bikerental;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;

import org.junit.jupiter.api.*;

public class ValuationPolicyTests {
    BikeType type1,type2,type3;   
    HashMap<BikeType,BigDecimal> testReplacementValues = new HashMap<BikeType,BigDecimal>();
    Bike bike1,bike2,bike3;
    Provider testProvider;

    
    @BeforeEach
    void setUp() throws Exception {
        this.testProvider = new Provider("Test Store", "64 Unit Street", "EH1 1EH", BigDecimal.ONE);
        
        this.type1 = new BikeType(new BigDecimal(0.1f), "type1");
        this.type2 = new BikeType(new BigDecimal(0.2f), "type2");
        this.type3 = new BikeType(new BigDecimal(0.3f), "type3");
        
        testReplacementValues.put(type1, new BigDecimal(1000));
        testReplacementValues.put(type2, new BigDecimal(2000));
        testReplacementValues.put(type3, new BigDecimal(3000));
        
        this.bike1 =  new Bike(LocalDate.now().minusYears(1), type1, "1",testProvider,testReplacementValues);
        this.bike2 =  new Bike(LocalDate.now().minusYears(2), type2, "2",testProvider,testReplacementValues);
        this.bike3 =  new Bike(LocalDate.now().minusYears(3), type3, "3",testProvider,testReplacementValues);
    }
    
    @Test
    void testLinearDepreciation() {
        BigDecimal calculation1 = Valuation.linearDepreciation(bike1,LocalDate.now(),testReplacementValues).setScale(0, RoundingMode.HALF_UP);
        assertEquals(calculation1, new BigDecimal(900));
        BigDecimal calculation2 = Valuation.linearDepreciation(bike2,LocalDate.now(),testReplacementValues).setScale(0, RoundingMode.HALF_UP);
        assertEquals(calculation2, new BigDecimal(1200));
        BigDecimal calculation3 = Valuation.linearDepreciation(bike3,LocalDate.now(),testReplacementValues).setScale(0, RoundingMode.HALF_UP);
        assertEquals(calculation3, new BigDecimal(300));
    }
    @Test
    void testDoubleDecliningDepreciation() {
        BigDecimal calculation1 = Valuation.doubleDecliningDepreciation(bike1, LocalDate.now(), testReplacementValues).setScale(0, RoundingMode.HALF_UP);
        assertEquals(calculation1,new BigDecimal(800));
        BigDecimal calculation2 = Valuation.doubleDecliningDepreciation(bike2, LocalDate.now(), testReplacementValues).setScale(0, RoundingMode.HALF_UP);
        assertEquals(calculation2,new BigDecimal(720));
        BigDecimal calculation3 = Valuation.doubleDecliningDepreciation(bike3, LocalDate.now(), testReplacementValues).setScale(0, RoundingMode.HALF_UP);
        assertEquals(calculation3,new BigDecimal(192));
    }
}
