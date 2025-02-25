package se.ltu.navigator.location;

import android.location.Location;

public class Room {

    private String id;
    private Location location;
    private int floor;
    public Room(String id, double longitude, double latitude, int floor) {
        this.id = id;
        this.floor = floor;
        this.location = new Location("");
        this.location.setLongitude(longitude);
        this.location.setLatitude(latitude);
        this.location.setAccuracy(0);
    }
    public Location getLocation() { return location; }
    public String getId() { return id; }
    public int getFloor() { return floor; }
}
