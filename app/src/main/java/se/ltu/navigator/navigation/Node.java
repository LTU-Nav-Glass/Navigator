package se.ltu.navigator.navigation;

import java.util.List;

public class Node {
    public enum Type {
        ROOM,
        HALLWAY,
        STAIRS,
        ELEVATOR,
        EXIT,
        TEMP
    }
    private String id;
    private double longitude;
    private double latitude;
    private int floor;
    private List<Edge> edges;
    private Type type;

    public Node(String id, double longitude, double latitude, int floor, Type type, List<Edge> edges) {
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.floor = floor;
        this.type = type;
        this.edges = edges;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }
    public Type getType() {
        return type;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }
    public void setType(Type type) {
        this.type = type;
    }

    public static class Edge {
        private String id;
        private double distance;

        public Edge(String id, double distance) {
            this.id = id;
            this.distance = distance;
        }

        // Getters and setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public double getDistance() {
            return distance;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }
    }
}
