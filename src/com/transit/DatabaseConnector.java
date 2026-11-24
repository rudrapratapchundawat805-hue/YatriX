package com.transit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;


public class DatabaseConnector {
    private static final String URL = "jdbc:mysql://localhost:3306/ahmedabad_transit?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    static {
        try {
            // Load MySQL JDBC driver (JDBC Architecture)
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Warning: MySQL JDBC Driver not found in classpath. Ensure mysql-connector-j jar is present.");
        }
    }


    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }


    public static int countRouteStops() throws SQLException {
        String query = "SELECT COUNT(*) AS total FROM route_stops";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next())
            {
                return rs.getInt("total");
            }
            return 0;
        }
    }


    public static void loadStations(
            HashMap<String, Integer> stationMap,
            HashMap<Integer, String> stationIdMap
    ) throws SQLException {
        String query = "SELECT station_id, station_name FROM stations";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("station_id");
                String name = rs.getString("station_name");
                stationMap.put(name.toLowerCase().trim(), id);
                stationIdMap.put(id, name);
            }
        }
    }

    public static void loadGraph(
            HashMap<Integer, ArrayList<com.transit.Connection>> adjList,
            HashMap<Integer, String> stationIdMap
    ) throws SQLException {
        String query =
                "SELECT rs.route_id, r.mode, r.route_no, r.route_name, r.headway_min, r.frequency_category, " +
                        "       rs.seq_no, rs.station_id, rs.arrival_time, rs.departure_time " +
                        "FROM route_stops rs " +
                        "JOIN routes r ON rs.route_id = r.route_id " +
                        "ORDER BY rs.route_id, rs.seq_no";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            int prevRouteId = -1;
            int prevStationId = -1;
            java.sql.Time prevDeparture = null;

            while (rs.next()) {
                int routeId = rs.getInt("route_id");
                String mode = rs.getString("mode");
                String routeNo = rs.getString("route_no");
                String routeName = rs.getString("route_name");
                int headwayMin = rs.getInt("headway_min");
                String frequencyCategory = rs.getString("frequency_category");
                int stationId = rs.getInt("station_id");
                java.sql.Time arrival = rs.getTime("arrival_time");
                java.sql.Time departure = rs.getTime("departure_time");

                if (routeId == prevRouteId) {
                    int timeMins = (int) ((arrival.getTime() - prevDeparture.getTime()) / 60000);
                    if (timeMins <= 0)
                    {
                        timeMins = 1;
                    }

                    String destName = stationIdMap.get(stationId);
                    if (destName != null) {
                        String frequencyInfo = "every ~" + headwayMin + " min (" + frequencyCategory + ")";
                        com.transit.Connection edge = new com.transit.Connection(
                                mode, routeNo, routeName, stationId, destName, 1, timeMins, frequencyInfo
                        );
                        adjList.computeIfAbsent(prevStationId, k -> new ArrayList<>()).add(edge);
                    }
                }

                prevRouteId = routeId;
                prevStationId = stationId;
                prevDeparture = departure;
            }
        }
    }
}