package se.ltu.navigator.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import se.ltu.navigator.MainActivity;


public class UserLocationManager
{
    private MainActivity mainActivity;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location location;
    private double longitude, latitude, altitude;

    //these two variables will be used for updating user during movement
    private final long TIME_BETWEEN_UPDATES = 5000;
    private final float MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 0;

    public UserLocationManager(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
        locationManager = (LocationManager) mainActivity.getSystemService(Context.LOCATION_SERVICE); //instantiated locationManager with the user's location information
        locationManager = (LocationManager) mainActivity.getSystemService(Context.LOCATION_SERVICE);
        startUpdates();
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

    /**
     * Starts location updates.
     */
    public void startUpdates() {
        // Check for permissions
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mainActivity.showPhoneStatePermission();
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, TIME_BETWEEN_UPDATES, MINIMUM_DISTANCE_CHANGE_FOR_UPDATES, this::setLocation);
    }

    /**
     * Stops location updates.
     */
    public void stopUpdates() {
        locationManager.removeUpdates(this::setLocation);
    }


    public void update(){
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mainActivity.showPhoneStatePermission();
            return;
        }

        setLocation(locationManager.getLastKnownLocation(locationManager.FUSED_PROVIDER)); //instantiate user based off phone's coordinates);
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

    public void setLocation(Location location) {
        if(location == null) return;
        this.location = location;
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        altitude = location.getAltitude();

        Log.i("ULM", "Updating localisation");
    }
}
