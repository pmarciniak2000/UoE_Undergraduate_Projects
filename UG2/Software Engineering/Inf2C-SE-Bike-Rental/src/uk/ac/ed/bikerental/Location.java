package uk.ac.ed.bikerental;

/**
* @author Patryk, Sean
* Represents a location which is made up of an address and postcode.
*/

public class Location {
    /** Represents the postcode, which forms part of the location.
    */
    private String postcode;
    /** Represents the word address, which forms part of the location.
     */
    private String address;
    
    /** Creates a location based on the postcode and address
     * Checks that the length of the postcode is greater or equal to 6.
     * @param postcode The postcode of the location.
     * @param address The word address of the location.
    */
    public Location(String postcode, String address) {
        assert postcode.length() >= 6;
        this.postcode = postcode;
        this.address = address;
    }
    /**
     * Checks if two locations are near each other
     * <p>
     * This method checks that the first two elements of both locations' 
     * postcodes are the same, if they are, two locations are said to be
     * near to each other.
     * @param other The other location to be compared with the original one.
     * @return Returns true if two locations are near each other.
     */
    public boolean isNearTo(Location other) {
       return ((this.postcode.substring(0,2)).equals((other.postcode.substring(0,2))));
    } 
    /** Gets location's postcode.
     * @return A string representing the location's postcode.
    */
    public String getPostcode() {
        return postcode;
    }
    /** Gets location's address.
     * @return A string representing the location's word address.
    */
    public String getAddress() {
        return address;
    }
}
