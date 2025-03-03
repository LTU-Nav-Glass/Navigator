package se.ltu.navigator.navigation;

import android.location.Location;

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
    private Location location;
    private int floor;
    private List<String> edges;
    private Type type;
    private double longitude;
    private double latitude;

    public Node(String id, double longitude, double latitude, int floor, Type type, List<String> edges) {
        this.id = id;
        this.location = new Location("");
        this.location.setLongitude(longitude);
        this.location.setLatitude(latitude);
        this.location.setAccuracy(0);
        this.floor = floor;
        this.type = type;
        this.edges = edges;
    }

    // Getters and setters
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public List<String> getEdges() {
        return edges;
    }

    public void setEdges(List<String> edges) {
        this.edges = edges;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}