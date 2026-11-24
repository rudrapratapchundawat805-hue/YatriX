package com.transit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TransitApp {

    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("                              "+ "🙏"+"Welcome to YatriX"+"🙏"+"                              ");
        try {
            String name;
            while (true){
                System.out.println("Enter your Name ");
                Scanner sc=new Scanner(System.in);
                name=sc.nextLine();
                if (name.matches("[a-zA-Z]+")) {
                    break;
                }
                System.out.println("invalid output ");
            }

            int age = 0;
            while (true) {
                try {
                    System.out.print("Enter your Age: ");
                    age = Integer.parseInt(reader.readLine());
                    if (age < 1 || age > 120) {
                        System.out.println("Please enter a valid age between 0 and 120.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter age as an integer.");
                }
            }

            double height = 0.0;
            while (true) {
                try {
                    System.out.print("Enter your Height in feet (e.g. 6.1): ");
                    height = Double.parseDouble(reader.readLine());
                    if (height < 0.5 || height > 9.0) {
                        System.out.println("Please enter a valid height between 0.5 and 9.0 feet.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter height as a decimal number.");
                }
            }

            char gender;

            while (true) {
                System.out.print("Enter your Gender (M/F/Male/Female): ");
                String genderInput = reader.readLine().trim();

                if (genderInput.equalsIgnoreCase("M") ||
                        genderInput.equalsIgnoreCase("Male")) {

                    gender = 'M';
                    break;

                } else if (genderInput.equalsIgnoreCase("F") ||
                        genderInput.equalsIgnoreCase("Female")) {

                    gender = 'F';
                    break;

                } else {
                    System.out.println("Invalid input! Please enter M, F, Male, or Female.");
                }
            }

            boolean hasMetroCard = false;

            while (true) {
                System.out.print("Do you have a Metro smart card? (Y/N/Yes/No): ");
                String cardInput = reader.readLine().trim();

                if (cardInput.equalsIgnoreCase("Y") ||
                        cardInput.equalsIgnoreCase("Yes")) {

                    hasMetroCard = true;
                    break;

                } else if (cardInput.equalsIgnoreCase("N") ||
                        cardInput.equalsIgnoreCase("No")) {

                    hasMetroCard = false;
                    break;

                } else {
                    System.out.println("Invalid input! Please enter only Y, Yes, N, or No.");
                }
            }
            UserSession user = new UserSession(name, age, height, gender, hasMetroCard);


            HashMap<String, Integer> stationMap = new HashMap<>();
            HashMap<Integer, String> stationIdMap = new HashMap<>();
            HashMap<Integer, ArrayList<Connection>> adjList = new HashMap<>();

            try {
                DatabaseConnector.loadStations(stationMap, stationIdMap);
                DatabaseConnector.loadGraph(adjList, stationIdMap);
            } catch (SQLException e) {
                System.err.println("\nCould not read the transit network from the database.");
                System.err.println("Details: " + e.getMessage());
                return;
            }

            if (stationMap.isEmpty()) {
                System.out.println("The database is connected but has no stations. Please load the CSV data first.");
                return;
            }

            System.out.println("Loaded " + stationIdMap.size() + " stations and their routes from the database.\n");
            System.out.println(user);

            boolean running = true;
            while (running) {
                System.out.println("\nType 'list' at the station prompt to see all available station names.");
                System.out.print("Enter Starting Point (e.g. Iskcon): ");
                String sourceInput = reader.readLine();

                if (sourceInput != null && sourceInput.trim().equalsIgnoreCase("list")) {
                    printStationList(stationIdMap);
                    System.out.print("Enter Starting Point (e.g. Iskcon): ");
                    sourceInput = reader.readLine();
                }

                System.out.print("Enter Destination Point (e.g. Kalupur): ");
                String destInput = reader.readLine();

                if (sourceInput == null || destInput == null || sourceInput.trim().isEmpty() || destInput.trim().isEmpty()) {
                    System.out.println("Source or Destination cannot be empty.");
                    continue;
                }

                String sourceName = sourceInput.trim();
                String destName = destInput.trim();

                try {
                    Integer sourceId = stationMap.get(sourceName.toLowerCase());
                    Integer destId = stationMap.get(destName.toLowerCase());

                    if (sourceId == null) {
                        throw new StationNotFoundException(sourceName);
                    }
                    if (destId == null) {
                        throw new StationNotFoundException(destName);
                    }

                    if (sourceId.equals(destId)) {
                        System.out.println("Starting point and destination are the same station. Fare: Rs 0.");
                        continue;
                    }

                    System.out.println("\nSearching for routes from '" + stationIdMap.get(sourceId)
                            + "' to '" + stationIdMap.get(destId) + "'...");


                    Vector<TransitResult> results = new Vector<>();

                    final int sId = sourceId;
                    final int dId = destId;
                    final String sName = stationIdMap.get(sourceId);
                    final String dName = stationIdMap.get(destId);

                    Thread directThread = new Thread(() -> {
                        String[] modes = {"AMTS", "BRTS", "METRO"};
                        for (String mode : modes)
                        {
                            try
                            {
                                PathNode destNode = RouteFinder.findBestRoute(sId, sName, dId, dName, mode, adjList);
                                TransitResult result = buildSingleModeResult(destNode, mode, user);
                                if (result != null)
                                {
                                    results.add(result);
                                }
                            }
                            catch (RouteNotFoundException e)
                            {
                                // No direct route on this mode - expected
                            }
                        }
                    }, "DirectRoute-Worker");

                    Thread multiThread = new Thread(() -> {
                        ArrayList<PathNode> allPaths = RouteFinder.findAllRoutes(sId, sName, dId, dName, adjList);
                        for (PathNode path : allPaths) {
                            TransitResult result = buildMultiModalResult(path, user);
                            if (result != null) {
                                results.add(result);
                            }
                        }
                    }, "MultiModal-Worker");

                    directThread.start();
                    multiThread.start();
                    directThread.join();
                    multiThread.join();

                    ArrayList<TransitResult> uniqueResults = removeDuplicateResults(results);

                    PriorityQueue<TransitResult> routePriorityQueue = new PriorityQueue<>();
                    routePriorityQueue.addAll(uniqueResults);

                    if (routePriorityQueue.isEmpty()) {
                        System.out.println("\nNo direct or connecting route found between '" + sourceName + "' and '" + destName + "'.");
                        System.out.println("These stations may not be connected by AMTS, BRTS or Metro in the current data.");
                    } else {
                        System.out.println("\nFound " + routePriorityQueue.size() + " route option(s), best fare and time first:");

                        LocalTime departureTime = LocalTime.now();
                        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("hh:mm a");

                        int rank = 1;
                        StringBuilder fileLogBuilder = new StringBuilder();
                        int maxDisplay = 5;

                        while (!routePriorityQueue.isEmpty() && rank <= maxDisplay) {
                            TransitResult route = routePriorityQueue.poll();
                            LocalTime arrivalTime = departureTime.plusMinutes(route.getTotalTimeMins());

                            String typeLabel = route.isMultiModal()
                                    ? "via transfer (" + route.getTransferCount() + " change" + (route.getTransferCount() > 1 ? "s" : "") + ")"
                                    : "direct";

                            String routeTitle = String.format(
                                    "\nOption %d - %s %s, %s",
                                    rank, route.getMode(), route.getRouteLabel(), typeLabel);

                            System.out.println(routeTitle);

                            if (route.isMultiModal() && !route.getSegments().isEmpty ()) {
                                System.out.println("Journey segments:");
                                for (int i = 0; i < route.getSegments().size(); i++) {
                                    RouteSegment seg = route.getSegments().get(i);
                                    System.out.println(seg.toString());
                                    if (i < route.getSegments().size() - 1) {
                                        System.out.println("      change at " + seg.getToStation() + " (about 5 min walk)");
                                    }
                                }
                            }

                            String pathDetails = String.format("Full path: %s", String.join(" -> ", route.getPathStations()));
                            System.out.println(pathDetails);

                            String stats = String.format(
                                    "Stops: %d | Time: %d mins (depart %s, arrive about %s)",
                                    route.getNumStops(), route.getTotalTimeMins(),
                                    departureTime.format(timeFormat), arrivalTime.format(timeFormat));
                            System.out.println(stats);

                            String fareInfo = String.format(
                                    "Standard fare: Rs %.2f | Your fare: Rs %.2f%nBreakdown: %s",
                                    route.getOriginalFare(), route.getDiscountedFare(), route.getFareBreakdown());
                            System.out.println(fareInfo);

                            fileLogBuilder.append(routeTitle).append("\n")
                                    .append(pathDetails).append("\n")
                                    .append(stats).append("\n")
                                    .append(fareInfo).append("\n\n");
                            rank++;
                        }

                        HistoryLogger.logSearch(user, stationIdMap.get(sourceId), stationIdMap.get(destId), fileLogBuilder.toString());
                        System.out.println("\nSearch logged to 'search_history.txt'.");
                    }

                } catch (StationNotFoundException e) {
                    System.out.println("\n" + e.getMessage());
                } catch (InterruptedException e) {
                    System.out.println("\nRoute search was interrupted: " + e.getMessage());
                }

                while (true)
                {
                    System.out.print("\nSearch another route? (Y/N): ");
                    String choice = reader.readLine();

                    if (choice.equalsIgnoreCase("Y") ||
                            choice.equalsIgnoreCase("Yes")) {

                        running = true;
                        break;

                    } else if (choice.equalsIgnoreCase("N") ||
                            choice.equalsIgnoreCase("No")) {

                        running = false;
                        System.out.println("🙏😊"+" Thank you for using YatriX"+"😊🙏");
                        break;
                    }
                    else
                    {
                        System.out.println("Invalid input! Please enter only Y, Yes, N, or No.");
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }

        System.out.println("\nThank you for using the Ahmedabad Transit Route Planner.");
    }


    private static void printStationList(HashMap<Integer, String> stationIdMap) {
        ArrayList<String> stationNames = new ArrayList<>(stationIdMap.values());
        java.util.Collections.sort(stationNames);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stationNames.size(); i++) {
            sb.append(stationNames.get(i));
            if (i < stationNames.size() - 1) sb.append(" | ");
            if ((i + 1) % 6 == 0) sb.append("\n");
        }
        System.out.println(sb);
    }


    private static TransitResult buildSingleModeResult(PathNode destNode, String mode, UserSession user) {
        if (destNode == null) return null;

        ArrayList<String> stations = new ArrayList<>();
        PathNode curr = destNode;
        String routeNo = "Unknown";
        String frequencyInfo = null;

        while (curr != null) {
            stations.add(0, curr.getStationName());
            if (curr.getViaConnection() != null) {
                routeNo = curr.getViaConnection().getRouteNo();
                frequencyInfo = curr.getViaConnection().getFrequencyInfo();
            }
            curr = curr.getParent();
        }

        int time = destNode.getCumulativeTime();
        int stops = destNode.getCumulativeStations();

        FareCalculator calculator = FareCalculator.getCalculator(mode);
        double discountedFare = calculator.calculateFare(stops, user);

        UserSession regularUser = new UserSession("Base Adult", 30, 5.5, 'M', false);
        double originalFare = calculator.calculateFare(stops, regularUser);

        String breakdown = calculator.getFareCalculationBreakdown(stops, user);

        String routeLabel = "Route " + routeNo + (frequencyInfo != null ? " (runs " + frequencyInfo + ")" : "");

        return new TransitResult(
                mode, routeLabel, stations, time, stops,
                originalFare, discountedFare, breakdown
        );
    }


    private static TransitResult buildMultiModalResult(PathNode destNode, UserSession user) {
        if (destNode == null) return null;

        LinkedList<PathNode> pathNodes = new LinkedList<>();
        PathNode curr = destNode;
        while (curr != null) {
            pathNodes.addFirst(curr);
            curr = curr.getParent();
        }

        ArrayList<String> allStations = new ArrayList<>();
        for (PathNode node : pathNodes) {
            allStations.add(node.getStationName());
        }

        ArrayList<RouteSegment> segments = new ArrayList<>();
        if (pathNodes.size() < 2) return null;

        String currentMode = null;
        String currentRouteNo = null;
        String currentFrequency = null;
        String segmentStartStation = pathNodes.getFirst().getStationName();
        ArrayList<String> segmentStations = new ArrayList<>();
        segmentStations.add(segmentStartStation);
        int segmentStops = 0;
        int segmentTime = 0;

        UserSession regularUser = new UserSession("Base Adult", 30, 5.5, 'M', false);
        double totalOriginalFare = 0.0;

        for (int i = 1; i < pathNodes.size(); i++) {
            PathNode node = pathNodes.get(i);
            Connection conn = node.getViaConnection();
            if (conn == null) continue;

            String mode = conn.getTransportType();
            String routeNo = conn.getRouteNo();

            if (currentMode == null) {
                currentMode = mode;
                currentRouteNo = routeNo;
            }

            if (!mode.equalsIgnoreCase(currentMode)) {
                FareCalculator calc = FareCalculator.getCalculator(currentMode);
                double segFinalFare = calc.calculateFare(segmentStops, user);
                String segBreakdown = calc.getFareCalculationBreakdown(segmentStops, user);
                totalOriginalFare += calc.calculateFare(segmentStops, regularUser);

                String segLabel = "Route " + currentRouteNo + (currentFrequency != null ? " (runs " + currentFrequency + ")" : "");
                segments.add(new RouteSegment(
                        currentMode, segLabel, segmentStartStation,
                        pathNodes.get(i - 1).getStationName(),
                        new ArrayList<>(segmentStations), segmentStops,
                        segmentTime, segFinalFare, segBreakdown
                ));

                currentMode = mode;
                currentRouteNo = routeNo;
                segmentStartStation = pathNodes.get(i - 1).getStationName();
                segmentStations = new ArrayList<>();
                segmentStations.add(segmentStartStation);
                segmentStops = 0;
                segmentTime = 0;
            }

            segmentStations.add(node.getStationName());
            segmentStops += conn.getNumStations();
            segmentTime += conn.getTimeMins();
            currentRouteNo = routeNo;
            currentFrequency = conn.getFrequencyInfo();
        }

        if (currentMode != null && segmentStops > 0) {
            FareCalculator calc = FareCalculator.getCalculator(currentMode);
            double segFinalFare = calc.calculateFare(segmentStops, user);
            String segBreakdown = calc.getFareCalculationBreakdown(segmentStops, user);
            totalOriginalFare += calc.calculateFare(segmentStops, regularUser);

            String segLabel = "Route " + currentRouteNo + (currentFrequency != null ? " (runs " + currentFrequency + ")" : "");
            segments.add(new RouteSegment(
                    currentMode, segLabel, segmentStartStation,
                    pathNodes.getLast().getStationName(),
                    new ArrayList<>(segmentStations), segmentStops,
                    segmentTime, segFinalFare, segBreakdown
            ));
        }

        int totalTime = destNode.getCumulativeTime();
        int totalStops = destNode.getCumulativeStations();
        int transferCount = destNode.getTransferCount();

        double totalDiscountedFare = 0.0;
        StringBuilder breakdownBuilder = new StringBuilder();

        for (int i = 0; i < segments.size(); i++) {
            RouteSegment seg = segments.get(i);
            totalDiscountedFare += seg.getFare();
            if (i > 0) breakdownBuilder.append(" + ");
            breakdownBuilder.append(seg.getMode()).append(": Rs ").append(String.format("%.1f", seg.getFare()));
        }

        StringBuilder modeLabel = new StringBuilder();
        for (int i = 0; i < segments.size(); i++) {
            if (i > 0) modeLabel.append(" -> ");
            modeLabel.append(segments.get(i).getMode());
        }

        StringBuilder routeLabel = new StringBuilder();
        for (int i = 0; i < segments.size(); i++) {
            if (i > 0) routeLabel.append(" + ");
            routeLabel.append(segments.get(i).getRouteLabel());
        }

        return new TransitResult(
                modeLabel.toString(), routeLabel.toString(), allStations,
                totalTime, totalStops,
                totalOriginalFare, totalDiscountedFare, breakdownBuilder.toString(),
                transferCount, segments
        );
    }


    private static ArrayList<TransitResult> removeDuplicateResults(Vector<TransitResult> results) {
        ArrayList<TransitResult> unique = new ArrayList<>();
        java.util.HashSet<String> seen = new java.util.HashSet<>();

        for (TransitResult r : results) {
            String key = r.getMode() + ":" + String.join(",", r.getPathStations());
            if (!seen.contains(key)) {
                seen.add(key);
                unique.add(r);
            }
        }
        return unique;
    }
}