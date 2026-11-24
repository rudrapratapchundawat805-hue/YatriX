package com.transit;

public class PathNode implements Comparable<PathNode> {
    private final int stationId;
    private final String stationName;
    private final int cumulativeTime;
    private final int cumulativeStations;
    private final PathNode parent;
    private final Connection viaConnection;
    private final int transferCount;  // number of mode changes so far

    public PathNode(int stationId, String stationName, int cumulativeTime,
                    int cumulativeStations, PathNode parent, Connection viaConnection) {
        this.stationId = stationId;
        this.stationName = stationName;
        this.cumulativeTime = cumulativeTime;
        this.cumulativeStations = cumulativeStations;
        this.parent = parent;
        this.viaConnection = viaConnection;

        // Calculate transfer count: if the mode changed from parent's connection to this connection
        if (parent != null && parent.getViaConnection() != null && viaConnection != null) {
            String prevMode = parent.getViaConnection().getTransportType();
            String currMode = viaConnection.getTransportType();
            if (!prevMode.equalsIgnoreCase(currMode)) {
                this.transferCount = parent.getTransferCount() + 1;
            } else {
                this.transferCount = parent.getTransferCount();
            }
        } else {
            this.transferCount = (parent != null) ? parent.getTransferCount() : 0;
        }
    }

    public int getStationId() {
        return stationId;
    }

    public String getStationName() {
        return stationName;
    }

    public int getCumulativeTime() {
        return cumulativeTime;
    }

    public int getCumulativeStations() {
        return cumulativeStations;
    }

    public PathNode getParent() {
        return parent;
    }

    public Connection getViaConnection() {
        return viaConnection;
    }

    public int getTransferCount() {
        return transferCount;
    }


    public String getCurrentMode() {
        if (viaConnection != null) {
            return viaConnection.getTransportType();
        }
        return "NONE";
    }

    @Override
    public int compareTo(PathNode other) {
        return Integer.compare(this.cumulativeTime, other.cumulativeTime);
    }
}