package uk.ac.ed.bikerental;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
* @author Patryk, Sean
* Represents a Date range which is made up of a start date and end date.
*/
public class DateRange {
    /** Represents the start and end date both of type LocalDate, which make up a date range.
     */
    private LocalDate start, end;
    /** Creates a date range based on the start and end date
     * @param start The start date of the date range.
     * @param end  The end date of the date range.
    */
    public DateRange(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end;
    }
    /** Gets the start date of the date range.
     * @return A LocalDate representing the start date of a date range
    */
    public LocalDate getStart() {
        return this.start;
    }
    /** Gets the end date of the date range.
     * @return A LocalDate representing the end date of a date range
    */
    public LocalDate getEnd() {
        return this.end;
    }
    /** Calculates the number of years between the start date and end date.
     * @return The number of years between the start date and end date.
    */
    public long toYears() {
        return ChronoUnit.YEARS.between(this.getStart(), this.getEnd());
    }
    /** Calculates the number of days between the start date and end date.
     * @return The number of days between the start date and end date.
    */
    public long toDays() {
        return ChronoUnit.DAYS.between(this.getStart(), this.getEnd());
    }
    /**
     * Checks if two date ranges overlap each other.
     * <p>
     * The method uses another method contains to check if dates are 
     * overlapping. If none of them do then two date ranges do not overlap.
     * @param other The other date range to be compared with the original one.
     * @return Returns true if two date ranges overlap.
     */
    public Boolean overlaps(DateRange other) {
        if (other == null) {
            throw new IllegalArgumentException("The other range cannot be null.");
        }
        return this.contains(other.getStart()) || this.contains(other.getEnd().minusDays(1))
            || other.contains(start) || other.contains(end.minusDays(1));  
    }
    /** Calculates a hash code value for the start and end date.
     * @return a hash code value for the start and end date.
    */
    @Override
    public int hashCode() {
        // hashCode method allowing use in collections
        return Objects.hash(end, start);
    }
    /**
     * Checks if two objects are equal to each other.
     * @param obj The object that is to be compared with the original one.
     * @return Returns true if both object are equal to each other.
     */
    @Override
    public boolean equals(Object obj) {
        // equals method for testing equality in tests
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DateRange other = (DateRange) obj;
        return Objects.equals(end, other.end) && Objects.equals(start, other.start);
    }
    /**
     * Checks if one date is contained in another.
     * <p>
     * This method checks if any there are any overlapping dates from both dates.
     * Throws illegal argument exception if the other date is null.
     * @param other The other date that is to be compared with the original date.
     * @return Returns true if there are any overlapping dates between the two dates. 
     */
    public boolean contains(LocalDate other) {
        if (other == null) {
            throw new IllegalArgumentException("The date cannot be null.");
        }
        return (other.isAfter(start) || other.isEqual(start)) && other.isBefore(end);
        }
}
