package com.transit;

import java.util.ArrayList;

import static java.lang.Integer.reverse;

public class TransitResult implements Comparable<TransitResult> {
    private final String mode;             // Primary mode or combined label for multi-modal
    private final String routeLabel;       // Route number/name or summary like "40/2 + Blue Line"
    private final ArrayList<String> pathStations;
    private final int totalTimeMins;
    private final int numStops;
    private final double originalFare;     // Standard adult fare
    private final double discountedFare;   // User-specific fare
    private final String fareBreakdown;
    private final int transferCount;       // Number of mode transfers (0 for direct)
    private final ArrayList<RouteSegment> segments;  // Per-segment details for multi-modal

    public TransitResult(String mode, String routeLabel, ArrayList<String> pathStations,
                         int totalTimeMins, int numStops,
                         double originalFare, double discountedFare, String fareBreakdown) {
        this(mode, routeLabel, pathStations, totalTimeMins, numStops,
                originalFare, discountedFare, fareBreakdown, 0, new ArrayList<>());
    }

    public TransitResult(String mode, String routeLabel, ArrayList<String> pathStations,
                         int totalTimeMins, int numStops,
                         double originalFare, double discountedFare, String fareBreakdown,
                         int transferCount, ArrayList<RouteSegment> segments) {
        this.mode = mode;
        this.routeLabel = routeLabel;
        this.pathStations = pathStations;
        this.totalTimeMins = totalTimeMins;
        this.numStops = numStops;
        this.originalFare = originalFare;
        this.discountedFare = discountedFare;
        this.fareBreakdown = fareBreakdown;
        this.transferCount = transferCount;
        this.segments = segments;
    }

    public String getMode() {
        return mode;
    }

    public String getRouteLabel() {
        return routeLabel;
    }

    public ArrayList<String> getPathStations() {
        return pathStations;
    }

    public int getTotalTimeMins() {
        return totalTimeMins;
    }

    public int getNumStops() {
        return numStops;
    }

    public double getOriginalFare() {
        return originalFare;
    }

    public double getDiscountedFare() {
        return discountedFare;
    }

    public String getFareBreakdown() {
        return fareBreakdown;
    }

    public int getTransferCount() {
        return transferCount;
    }

    public ArrayList<RouteSegment> getSegments() {
        return segments;
    }

    public boolean isMultiModal() {
        return transferCount > 0;
    }

    @Override
    public int compareTo(TransitResult other) {
        // Compare by discounted fare first (Cheapest route is preferred)
        int fareCompare = Double.compare(
                this.discountedFare,
                other.discountedFare
        );
        if (fareCompare != 0) {
            return fareCompare;
        }
        // If fares are equal, compare by travel time (Shortest duration is preferred)
        int timeCompare = Integer.compare(this.totalTimeMins, other.totalTimeMins);
        if (timeCompare != 0) {
            return timeCompare;
        }
        // If time is also equal, prefer fewer transfers
        return Integer.compare(this.transferCount, other.transferCount);
    }
}

class RouteSegment {
    private final String mode;
    private final String routeLabel;
    private final String fromStation;
    private final String toStation;
    private final ArrayList<String> intermediateStations;
    private final int numStops;
    private final int timeMins;
    private final double fare;
    private final String fareBreakdown;

    public RouteSegment(String mode, String routeLabel, String fromStation, String toStation,
                        ArrayList<String> intermediateStations, int numStops,
                        int timeMins, double fare, String fareBreakdown) {
        this.mode = mode;
        this.routeLabel = routeLabel;
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.intermediateStations = intermediateStations;
        this.numStops = numStops;
        this.timeMins = timeMins;
        this.fare = fare;
        this.fareBreakdown = fareBreakdown;
    }

    public String getMode() { return mode; }
    public String getRouteLabel() { return routeLabel; }
    public String getFromStation() { return fromStation; }
    public String getToStation() { return toStation; }
    public ArrayList<String> getIntermediateStations() { return intermediateStations; }
    public int getNumStops() { return numStops; }
    public int getTimeMins() { return timeMins; }
    public double getFare() { return fare; }
    public String getFareBreakdown() { return fareBreakdown; }

    @Override
    public String toString() {
        return String.format("    %s %s: %s to %s (%d stops, %d mins, Rs %.1f)",
                mode, routeLabel, fromStation, toStation, numStops, timeMins, fare);
    }
}