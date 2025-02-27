package se.ltu.navigator.navigation;

import android.content.Context;
import android.content.res.AssetManager;
import android.location.Location;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.IOUtils;

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
            String json = IOUtils.toString(inputStream);
            inputStream.close();

            Gson gson = new Gson();
            Type nodeListType = new TypeToken<List<Node>>() {}.getType();
            List<Node> nodeList = gson.fromJson(json, nodeListType);

            nodes = new HashMap<>();
            for (Node node : nodeList) {
                if(node.getLocation() == null) {
                    Location location = new Location("");
                    location.setLongitude(node.getLongitude());
                    location.setLatitude(node.getLatitude());
                    node.setLocation(location);
                }
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
        String tempNodeId = "temp_start";
        insertNodeAtClosestEdge(startLongitude, startLatitude, null, tempNodeId);

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

            for (String edge : currentNode.getEdges()) {
                Node adjacentNode = nodes.get(edge);
                double newDist = distances.get(currentNode.getId()) + currentNode.getLocation().distanceTo(adjacentNode.getLocation());

                if (newDist < distances.get(adjacentNode.getId())) {
                    distances.put(adjacentNode.getId(), newDist);
                    previousNodes.put(adjacentNode.getId(), currentNode.getId());
                    priorityQueue.add(new NodeDistance(adjacentNode.getId(), newDist));
                }
            }
        }

        List<Node> path = new ArrayList<>();
        for (String at = targetNodeId; at != null; at = previousNodes.get(at)) {
            path.add(nodes.get(at));
        }
        Collections.reverse(path);

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

        Location tempLocation = new Location("");
        tempLocation.setLongitude(longitude);
        tempLocation.setLatitude(latitude);

        for (Node node : nodes.values()) {
            if (node.getType() == Node.Type.STAIRS) {
                continue;
            }
            for (String edge : node.getEdges()) {
                Node adjacentNode = nodes.get(edge);
                if (adjacentNode.getType() == Node.Type.STAIRS) {
                    continue;
                }
                double[] closestPoint = findClosestPointOnEdge(node, adjacentNode, longitude, latitude);
                Location closestLocation = new Location("");
                closestLocation.setLongitude(closestPoint[0]);
                closestLocation.setLatitude(closestPoint[1]);
                double distance = tempLocation.distanceTo(closestLocation);

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

            tempNode.getEdges().add(closestNode1[0].getId());
            tempNode.getEdges().add(closestNode2[0].getId());

            closestNode1[0].getEdges().removeIf(edge -> edge.equals(closestNode2[0].getId()));
            closestNode2[0].getEdges().removeIf(edge -> edge.equals(closestNode1[0].getId()));

            closestNode1[0].getEdges().add(newNodeId);
            closestNode2[0].getEdges().add(newNodeId);

            if (inputNodeId != null) {
                Node inputNode = new Node(inputNodeId, longitude, latitude, closestNode1[0].getFloor(), Node.Type.ROOM, new ArrayList<>());
                nodes.put(inputNodeId, inputNode);
                inputNode.getEdges().add(newNodeId);
                tempNode.getEdges().add(inputNodeId);
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
            List<String> tempEdges = tempNode.getEdges();

            if (tempEdges.size() >= 2) {
                // We can assume that there are always two edges because a temp node
                // is always inserted between two nodes
                String edge1 = tempEdges.get(0);
                String edge2 = tempEdges.get(1);

                Node node1 = nodes.get(edge1);
                Node node2 = nodes.get(edge2);

                node1.getEdges().removeIf(edge -> edge.equals(tempNodeId));
                node2.getEdges().removeIf(edge -> edge.equals(tempNodeId));

                node1.getEdges().add(node2.getId());
                node2.getEdges().add(node1.getId());

                // Check for a room node
                if (tempEdges.size() == 3) {
                    String edge3 = tempEdges.get(2);
                    Node roomNode = nodes.get(edge3);

                    if(roomNode != null) {
                        roomNode.getEdges().removeIf(edge -> edge.equals(tempNodeId));

                        if (roomNode.getType() == Node.Type.ROOM) {
                            roomNodeIdsToRemove.add(roomNode.getId());
                        }
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
        double x1 = node1.getLocation().getLongitude();
        double y1 = node1.getLocation().getLatitude();
        double x2 = node2.getLocation().getLongitude();
        double y2 = node2.getLocation().getLatitude();

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
