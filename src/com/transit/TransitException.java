package com.transit;

/**
 * Base custom checked exception for the transit system (Syllabus: Checked & Unchecked exceptions).
 */
public class TransitException extends Exception {
    public TransitException(String message) {
        super(message);
    }
}

/**
 * Exception thrown when a station is not found in the database.
 */
class StationNotFoundException extends TransitException {
    public StationNotFoundException(String stationName) {
        super("Station '" + stationName + "' not found in the transit database.");
    }
}

/**
 * Exception thrown when no route exists between source and destination.
 */
class RouteNotFoundException extends TransitException {
    public RouteNotFoundException(String source, String dest, String mode) {
        super("No route found from '" + source + "' to '" + dest + "' using " + mode + ".");
    }
}