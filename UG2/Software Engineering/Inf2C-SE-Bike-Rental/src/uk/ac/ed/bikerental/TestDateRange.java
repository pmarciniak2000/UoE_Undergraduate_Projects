package uk.ac.ed.bikerental;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestDateRange {
    private DateRange dateRange1, dateRange2, dateRange3, dateRange4;

    @BeforeEach
    void setUp() throws Exception {
        // Setup resources before each test
        this.dateRange1 = new DateRange(LocalDate.of(2019, 1, 7),
                LocalDate.of(2019, 1, 10));
        this.dateRange2 = new DateRange(LocalDate.of(2019, 1, 5),
                LocalDate.of(2019, 1, 23));
        this.dateRange3 = new DateRange(LocalDate.of(2015, 1, 7),
                LocalDate.of(2018, 1, 10));
        this.dateRange4 = new DateRange(null, null);
    }

    // Sample JUnit tests checking toYears works
    @Test
    void testToYears1() {
        assertEquals(0, this.dateRange1.toYears());
    }

    @Test
    void testToYears3() {
        assertEquals(3, this.dateRange3.toYears());
    }

    @Test
    void testOverlapsTrue() {
        // TODO: check we can see when two date ranges overlap
        assertTrue(this.dateRange1.overlaps(this.dateRange2));
      }

    @Test
    void testOverlapsFalse() {
        // TODO: check we can see when two date ranges  don't overlap
        assertFalse(this.dateRange2.overlaps(this.dateRange3));
    }

    // TODO: put some of your own unit tests here
    
    @Test
    void testNullDate() {
        // TODO: checks to make sure a date range is not null
        assertNotNull(this.dateRange4);
    }
    
}
