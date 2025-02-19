package se.ltu.navigator.navigation;

import android.content.Context;
import android.content.res.AssetManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Graph {
    private Map<String, Node> nodes;

    public Graph(Context context, String filename) {
        loadGraphFromJson(context, filename);
    }
    private void loadGraphFromJson(Context context, String filename) {
        AssetManager assetManager = context.getAssets();
        try (InputStream inputStream = assetManager.open(filename)) {
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            String json = new String(buffer, StandardCharsets.UTF_8);

            Gson gson = new Gson();
            Type nodeListType = new TypeToken<List<Node>>() {}.getType();
            List<Node> nodeList = gson.fromJson(json, nodeListType);

            nodes = new HashMap<>();
            for (Node node : nodeList) {
                nodes.put(node.getId(), node);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * THIS ASSUMES YOU ARE IN THE BUILDING
     *
     * Function for finding the shortest path on the graph given a longitude and latitude
     * starting position. The function uses Dijkstra's algorithm to find the shortest path.
     * And returns a list of nodes representing the path.
     * @param startLongitude The longitude of the starting position.
     * @param startLatitude The latitude of the starting position.
     * @param targetNodeId The ID of the target node.
     * @return A list of nodes representing the shortest path.
     *
     * TODO: Maybe some sort of outside the building check must be made. Then if the user is outside
     * TODO: the building the user should be directed to the nearest entrance.
     */
    public List<Node> findShortestPath(double startLongitude, double startLatitude, String targetNodeId) {
        // Insert a temporary node at the closest edge to the starting position
        String tempNodeId = "temp_start";
        insertNodeAtClosestEdge(startLongitude, startLatitude, null, tempNodeId);

        // Use Dijkstra's algorithm to find the shortest path
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> previousNodes = new HashMap<>();
        PriorityQueue<NodeDistance> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(NodeDistance::getDistance));

        for (String nodeId : nodes.keySet()) {
            distances.put(nodeId, Double.MAX_VALUE);
        }
        distances.put(tempNodeId, 0.0);
        priorityQueue.add(new NodeDistance(tempNodeId, 0.0));

        while (!priorityQueue.isEmpty()) {
            NodeDistance current = priorityQueue.poll();
            Node currentNode = nodes.get(current.getNodeId());

            if (currentNode.getId().equals(targetNodeId)) {
                break;
            }

            for (Node.Edge edge : currentNode.getEdges()) {
                Node adjacentNode = nodes.get(edge.getId());
                double newDist = distances.get(currentNode.getId()) + edge.getDistance();

                if (newDist < distances.get(adjacentNode.getId())) {
                    distances.put(adjacentNode.getId(), newDist);
                    previousNodes.put(adjacentNode.getId(), currentNode.getId());
                    priorityQueue.add(new NodeDistance(adjacentNode.getId(), newDist));
                }
            }
        }

        // Reconstruct the shortest path
        List<Node> path = new ArrayList<>();
        for (String at = targetNodeId; at != null; at = previousNodes.get(at)) {
            path.add(nodes.get(at));
        }
        Collections.reverse(path);

        // Clean up the temporary node
        nodes.remove(tempNodeId);

        return path;
    }

    /**
     * Insert a new node at the closest edge to a given point. Will also insert the given position
     * as a room if an input node ID is provided. Otherwise only the node on the edge will be inserted.
     * @param longitude The longitude of the point.
     * @param latitude The latitude of the point.
     * @param inputNodeId The ID of the input node.
     * @param newNodeId The ID of the new node.
     */
    public void insertNodeAtClosestEdge(double longitude, double latitude, String inputNodeId, String newNodeId) {
        final Node[] closestNode1 = {null};
        final Node[] closestNode2 = {null};
        double minDistance = Double.MAX_VALUE;
        double closestLongitude = 0;
        double closestLatitude = 0;

        for (Node node : nodes.values()) {
            for (Node.Edge edge : node.getEdges()) {
                Node adjacentNode = nodes.get(edge.getId());
                double[] closestPoint = findClosestPointOnEdge(node, adjacentNode, longitude, latitude);
                double distance = haversineDistance(longitude, latitude, closestPoint[0], closestPoint[1]);

                if (distance < minDistance) {
                    minDistance = distance;
                    closestNode1[0] = node;
                    closestNode2[0] = adjacentNode;
                    closestLongitude = closestPoint[0];
                    closestLatitude = closestPoint[1];
                }
            }
        }

        if (closestNode1[0] != null && closestNode2[0] != null) {
            Node tempNode = new Node(newNodeId, closestLongitude, closestLatitude, closestNode1[0].getFloor(), Node.Type.TEMP, new ArrayList<>());
            nodes.put(newNodeId, tempNode);

            double distanceToNode1 = haversineDistance(closestLongitude, closestLatitude, closestNode1[0].getLongitude(), closestNode1[0].getLatitude());
            double distanceToNode2 = haversineDistance(closestLongitude, closestLatitude, closestNode2[0].getLongitude(), closestNode2[0].getLatitude());

            tempNode.getEdges().add(new Node.Edge(closestNode1[0].getId(), distanceToNode1));
            tempNode.getEdges().add(new Node.Edge(closestNode2[0].getId(), distanceToNode2));

            closestNode1[0].getEdges().removeIf(edge -> edge.getId().equals(closestNode2[0].getId()));
            closestNode2[0].getEdges().removeIf(edge -> edge.getId().equals(closestNode1[0].getId()));

            closestNode1[0].getEdges().add(new Node.Edge(newNodeId, distanceToNode1));
            closestNode2[0].getEdges().add(new Node.Edge(newNodeId, distanceToNode2));

            if (inputNodeId != null) {
                Node inputNode = new Node(inputNodeId, longitude, latitude, closestNode1[0].getFloor(), Node.Type.ROOM, new ArrayList<>());
                nodes.put(inputNodeId, inputNode);

                double distanceToTempNode = haversineDistance(longitude, latitude, closestLongitude, closestLatitude);
                inputNode.getEdges().add(new Node.Edge(newNodeId, distanceToTempNode));
                tempNode.getEdges().add(new Node.Edge(inputNodeId, distanceToTempNode));
            }
        }
    }

    /**
     * Cleans the graph by removing temporary nodes. This removes any rooms connecting to temporary nodes.
     * It also reconnects any connections which were removed by the introduction of the temporary node.
     */
    public void cleanGraph() {
        List<String> tempNodeIds = new ArrayList<>();
        List<String> roomNodeIdsToRemove = new ArrayList<>();

        // Collect all temporary nodes
        for (Node node : nodes.values()) {
            if (node.getType() == Node.Type.TEMP) {
                tempNodeIds.add(node.getId());
            }
        }

        // Remove temporary nodes and reconnect edges
        for (String tempNodeId : tempNodeIds) {
            Node tempNode = nodes.get(tempNodeId);
            List<Node.Edge> tempEdges = tempNode.getEdges();

            if (tempEdges.size() >= 2) {
                // We can assume that there are always two edges because a temp node
                // is always inserted between two nodes
                Node.Edge edge1 = tempEdges.get(0);
                Node.Edge edge2 = tempEdges.get(1);

                Node node1 = nodes.get(edge1.getId());
                Node node2 = nodes.get(edge2.getId());

                double newDistance = edge1.getDistance() + edge2.getDistance();

                node1.getEdges().removeIf(edge -> edge.getId().equals(tempNodeId));
                node2.getEdges().removeIf(edge -> edge.getId().equals(tempNodeId));

                node1.getEdges().add(new Node.Edge(node2.getId(), newDistance));
                node2.getEdges().add(new Node.Edge(node1.getId(), newDistance));

                // Check for a room node
                if (tempEdges.size() == 3) {
                    Node.Edge edge3 = tempEdges.get(2);
                    Node roomNode = nodes.get(edge3.getId());

                    roomNode.getEdges().removeIf(edge -> edge.getId().equals(tempNodeId));

                    if (roomNode.getType() == Node.Type.ROOM) {
                        roomNodeIdsToRemove.add(roomNode.getId());
                    }
                }
            }

            nodes.remove(tempNodeId);
        }

        // Remove room nodes connected to temporary nodes
        for (String roomNodeId : roomNodeIdsToRemove) {
            nodes.remove(roomNodeId);
        }
    }

    /**
     * Find the point on the edge between two nodes that is closest to a given point.
     * @param node1 The first node.
     * @param node2 The second node.
     * @param longitude The longitude of the point.
     * @param latitude The latitude of the point.
     * @return The coordinates of the point on the edge closest to the given point.
     */
    private double[] findClosestPointOnEdge(Node node1, Node node2, double longitude, double latitude) {
        double x1 = node1.getLongitude();
        double y1 = node1.getLatitude();
        double x2 = node2.getLongitude();
        double y2 = node2.getLatitude();

        double A = longitude - x1;
        double B = latitude - y1;
        double C = x2 - x1;
        double D = y2 - y1;

        double dot = A * C + B * D;
        double lenSq = C * C + D * D;
        double param = dot / lenSq;

        double closestX, closestY;

        if (param < 0 || (x1 == x2 && y1 == y2)) {
            closestX = x1;
            closestY = y1;
        } else if (param > 1) {
            closestX = x2;
            closestY = y2;
        } else {
            closestX = x1 + param * C;
            closestY = y1 + param * D;
        }

        return new double[]{closestX, closestY};
    }

    /**
     * Calculate the distance between two points on the Earth's surface using the Haversine formula.
     * @param lon1 The longitude of the first point.
     * @param lat1 The latitude of the first point.
     * @param lon2 The longitude of the second point.
     * @param lat2 The latitude of the second point.
     * @return The distance between the two points in kilometers.
     */
    private double haversineDistance(double lon1, double lat1, double lon2, double lat2) {
        final int R = 6371; // Radius of the Earth in kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public Node getNodeById(String id) {
        return nodes.get(id);
    }
    public Map<String, Node> getNodes() {
        return nodes;
    }

    private static class NodeDistance {
        private final String nodeId;
        private final double distance;

        public NodeDistance(String nodeId, double distance) {
            this.nodeId = nodeId;
            this.distance = distance;
        }

        public String getNodeId() {
            return nodeId;
        }

        public double getDistance() {
            return distance;
        }
    }
}
