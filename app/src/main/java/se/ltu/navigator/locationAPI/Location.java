package se.ltu.navigator.locationAPI;

public class Location {
    private String id;
    private float longitude;
    private float latitude;

    public Location(String id, float longitude, float latitude) {
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getId() {
        return id;
    }

    public float getLongitude() {
        return longitude;
    }

    public float getLatitude() {
        return latitude;
    }
}