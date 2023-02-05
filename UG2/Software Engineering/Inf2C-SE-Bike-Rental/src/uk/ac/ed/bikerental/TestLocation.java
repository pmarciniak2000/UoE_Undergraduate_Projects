package uk.ac.ed.bikerental;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;


class TestLocation {
    Location location1,location2,location3,location4;
    
    @BeforeEach
    void setUp() throws Exception {
        location1 = new Location("EH10 4BF","44-46 Morningside Road");
        location2 = new Location("W1T 1JY","14 Tottenham Court Road");
        location3 = new Location("B30 2EW","27 Colmore Row");
        location4 = new Location("B31 2NW","91 Western Road");
    }
    
    @Test
    void testShortPostcodeError() {
        assertThrows(AssertionError.class, () -> {
            new Location("B31","91 Western Road");
        });
    }
    @Test
    void testNotNear() {
        assertFalse(location1.isNearTo(location2));
    }
    @Test
    void testIsNear() {
        assertTrue(location3.isNearTo(location4));
    }
    @Test
    void testInvalidEntryNear() {
        assertThrows(AssertionError.class, () -> {
            location1.isNearTo(new Location("B31","91 Western Road"));
        });
     }
}
