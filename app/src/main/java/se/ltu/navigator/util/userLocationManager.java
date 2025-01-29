package se.ltu.navigator.util;

import android.location.Location;


public class userLocationManager
{
    private Location location;
    private double longitude, latitude, altitude;
    public userLocationManager(Location location)
    {
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        altitude = location.getAltitude();
        this.location = location;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getAltitude()
    {
        return altitude;
    }

    public Location getLocation()
    {
        return location;
    }

    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public void setAltitude(double altitude)
    {
        this.altitude = altitude;
    }

    public void setLocation(Location location)
    {
        this.location = location;
    }


}
