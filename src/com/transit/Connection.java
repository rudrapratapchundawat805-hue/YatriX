package com.transit;

public class Connection{
    private final String transportType;
    private final String routeNo;
    private final String routeName;
    private final int destStationId;
    private final String destStationName;
    private final int numStations;
    private final int timeMins;
    private final String frequencyInfo;

    public Connection(String transportType, String routeNo, String routeName, int destStationId,
                      String destStationName, int numStations, int timeMins, String frequencyInfo) {
        this.transportType = transportType;
        this.routeNo = routeNo;
        this.routeName = routeName;
        this.destStationId = destStationId;
        this.destStationName = destStationName;
        this.numStations = numStations;
        this.timeMins = timeMins;
        this.frequencyInfo = frequencyInfo;
    }

    public String getTransportType() {
        return transportType;
    }

    public String getRouteNo() {
        return routeNo;
    }

    public String getRouteName() {
        return routeName;
    }

    public int getDestStationId() {
        return destStationId;
    }

    public String getDestStationName() {
        return destStationName;
    }

    public int getNumStations() {
        return numStations;
    }

    public int getTimeMins() {
        return timeMins;
    }

    public String getFrequencyInfo() {
        return frequencyInfo;
    }
}