package se.ltu.navigator.locationAPI;

import android.location.Location;

public class Room {

    private String id;
    private Location location;
    public Room(String id, double longitude, double latitude) {
        this.id = id;
        this.location = new Location("");
        this.location.setLongitude(longitude);
        this.location.setLatitude(latitude);
        this.location.setAccuracy(0);
    }
    public Location getLocation() { return location; }
    public String getId() { return id; }
}
