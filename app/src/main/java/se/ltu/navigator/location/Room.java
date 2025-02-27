package se.ltu.navigator.location;

import android.location.Location;

public class Room {

    private final String id;
    private final Location location;
    private final int floor;
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
