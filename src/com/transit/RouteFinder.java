package com.transit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class RouteFinder {

    private static final int MAX_ROUTES = 5;
    private static final int MAX_TRANSFERS = 3;

    public static PathNode findBestRoute(
            int sourceId,
            String sourceName,
            int destId,
            String destName,
            String mode,
            HashMap<Integer, ArrayList<Connection>> adjList
    ) throws RouteNotFoundException {

        PriorityQueue<PathNode> pq = new PriorityQueue<>();
        HashMap<Integer, Integer> minTimeMap = new HashMap<>();


        PathNode startNode = new PathNode(sourceId, sourceName, 0, 0, null, null);
        pq.add(startNode);
        minTimeMap.put(sourceId, 0);

        while (!pq.isEmpty()) {
            PathNode current = pq.poll();


            if (current.getStationId() == destId) {
                return current;
            }

            if (current.getCumulativeTime() > minTimeMap.getOrDefault(current.getStationId(), Integer.MAX_VALUE)) {
                continue;
            }

            ArrayList<Connection> neighbors = adjList.get(current.getStationId());
            if (neighbors != null) {
                for (Connection conn : neighbors) {
                    if (conn.getTransportType().equalsIgnoreCase(mode)) {
                        int nextTime = current.getCumulativeTime() + conn.getTimeMins();

                        if (nextTime < minTimeMap.getOrDefault(conn.getDestStationId(), Integer.MAX_VALUE)) {
                            minTimeMap.put(conn.getDestStationId(), nextTime);
                            PathNode nextNode = new PathNode(
                                    conn.getDestStationId(),
                                    conn.getDestStationName(),
                                    nextTime,
                                    current.getCumulativeStations() + conn.getNumStations(),
                                    current,
                                    conn
                            );
                            pq.add(nextNode);
                        }
                    }
                }
            }
        }

        throw new RouteNotFoundException(sourceName, destName, mode);
    }

    public static PathNode findMultiModalRoute(
            int sourceId,
            String sourceName,
            int destId,
            String destName,
            HashMap<Integer, ArrayList<Connection>> adjList
    ) {
        PriorityQueue<PathNode> pq = new PriorityQueue<>();
        HashMap<String, Integer> minTimeMap = new HashMap<>();

        PathNode startNode = new PathNode(sourceId, sourceName, 0, 0, null, null);
        pq.add(startNode);
        minTimeMap.put(sourceId + "-NONE", 0);

        while (!pq.isEmpty()) {
            PathNode current = pq.poll();

            if (current.getStationId() == destId) {
                return current;
            }

            String currentKey = current.getStationId() + "-" + current.getCurrentMode();
            if (current.getCumulativeTime() > minTimeMap.getOrDefault(currentKey, Integer.MAX_VALUE)) {
                continue;
            }

            ArrayList<Connection> neighbors = adjList.get(current.getStationId());
            if (neighbors != null) {
                for (Connection conn : neighbors) {
                    int newTransfers = current.getTransferCount();
                    if (current.getViaConnection() != null &&
                            !current.getViaConnection().getTransportType().equalsIgnoreCase(conn.getTransportType())) {
                        newTransfers++;
                    }
                    if (newTransfers > MAX_TRANSFERS) continue;

                    int nextTime = current.getCumulativeTime() + conn.getTimeMins();
                    String nextKey = conn.getDestStationId() + "-" + conn.getTransportType();

                    if (nextTime < minTimeMap.getOrDefault(nextKey, Integer.MAX_VALUE)) {
                        minTimeMap.put(nextKey, nextTime);
                        PathNode nextNode = new PathNode(
                                conn.getDestStationId(),
                                conn.getDestStationName(),
                                nextTime,
                                current.getCumulativeStations() + conn.getNumStations(),
                                current,
                                conn
                        );
                        pq.add(nextNode);
                    }
                }
            }
        }
        return null;
    }

    public static ArrayList<PathNode> findAllRoutes(
            int sourceId,
            String sourceName,
            int destId,
            String destName,
            HashMap<Integer, ArrayList<Connection>> adjList
    ) {
        ArrayList<PathNode> allRoutes = new ArrayList<>();

        String[] modes = {"AMTS", "BRTS", "METRO"};
        for (String mode : modes) {
            try {
                PathNode route = findBestRoute(sourceId, sourceName, destId, destName, mode, adjList);
                allRoutes.add(route);
            } catch (RouteNotFoundException e) {

            }
        }

        ArrayList<PathNode> multiModalRoutes = findKShortestMultiModal(
                sourceId, sourceName, destId, destName, adjList, MAX_ROUTES
        );

        for (PathNode mmRoute : multiModalRoutes) {
            if (!isDuplicatePath(allRoutes, mmRoute)) {
                allRoutes.add(mmRoute);
            }
        }

        allRoutes.sort((a,b) -> Integer.compare(a.getCumulativeTime(), b.getCumulativeTime()));

        while (allRoutes.size() > MAX_ROUTES) {
            allRoutes.remove(allRoutes.size() - 1);
        }

        return allRoutes;
    }

    private static ArrayList<PathNode> findKShortestMultiModal(
            int sourceId,
            String sourceName,
            int destId,
            String destName,
            HashMap<Integer, ArrayList<Connection>> adjList,
            int k
    ) {
        ArrayList<PathNode> results = new ArrayList<>();
        PriorityQueue<PathNode> pq = new PriorityQueue<>();
        HashMap<String, Integer> visitCount = new HashMap<>();

        PathNode startNode = new PathNode(sourceId, sourceName, 0, 0, null, null);
        pq.add(startNode);

        while (!pq.isEmpty() && results.size() < k) {
            PathNode current = pq.poll();

            String key = current.getStationId() + "-" + current.getCurrentMode();
            int count = visitCount.getOrDefault(key, 0);

            if (count >= k) continue;
            visitCount.put(key, count + 1);

            if (current.getStationId() == destId) {
                results.add(current);
                continue;
            }

            ArrayList<Connection> neighbors = adjList.get(current.getStationId());
            if (neighbors != null) {
                for (Connection conn : neighbors) {
                    int newTransfers = current.getTransferCount();
                    if (current.getViaConnection() != null &&
                            !current.getViaConnection().getTransportType().equalsIgnoreCase(conn.getTransportType())) {
                        newTransfers++;
                    }
                    if (newTransfers > MAX_TRANSFERS) continue;

                    if (current.getParent() != null &&
                            conn.getDestStationId() == current.getParent().getStationId()) {
                        continue;
                    }

                    int nextTime = current.getCumulativeTime() + conn.getTimeMins();
                    PathNode nextNode = new PathNode(
                            conn.getDestStationId(),
                            conn.getDestStationName(),
                            nextTime,
                            current.getCumulativeStations() + conn.getNumStations(),
                            current,
                            conn
                    );
                    pq.add(nextNode);
                }
            }
        }

        return results;
    }

    private static boolean isDuplicatePath(ArrayList<PathNode> existing, PathNode newRoute) {
        ArrayList<Integer> newPath = extractStationIds(newRoute);
        for (PathNode ex : existing) {
            ArrayList<Integer> existingPath = extractStationIds(ex);
            if (newPath.equals(existingPath)) {
                return true;
            }
        }
        return false;
    }

    private static ArrayList<Integer> extractStationIds(PathNode endNode) {
        LinkedList<Integer> ids = new LinkedList<>();
        PathNode curr = endNode;
        while (curr != null) {
            ids.addFirst(curr.getStationId());
            curr = curr.getParent();
        }
        return new ArrayList<>(ids);
    }
}