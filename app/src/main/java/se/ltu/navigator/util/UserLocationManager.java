package se.ltu.navigator.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import androidx.core.app.ActivityCompat;

import se.ltu.navigator.MainActivity;


public class UserLocationManager
{
    private Location location;
    private double longitude, latitude, altitude;

    //these two variables will be used for updating user during movement
    private final long MINIMUM_TIME_BETWEEN_UPDATES = 55;
    private final float MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 3;
    private LocationListener locationListener; //able to give current updates to locationManager
    private MainActivity mainActivity;
    private LocationManager locationManager;

    public interface LocationUpdateListener {
        void onLocationUpdated(Location location);
    }

    private LocationUpdateListener locationUpdateListener;

    //private Context context;
    public UserLocationManager(Location location)
    {
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        altitude = location.getAltitude();
        this.location = location;
    }

    public UserLocationManager(MainActivity mainActivity, LocationUpdateListener listener)
    {
        this.mainActivity = mainActivity;
        this.locationUpdateListener = listener;
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

    public void startUpdates() {
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mainActivity.showPhoneStatePermission();
            return;
        }

        locationListener = location -> {
            setLocation(location);
            if(locationUpdateListener != null) {
                locationUpdateListener.onLocationUpdated(location);
            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MINIMUM_TIME_BETWEEN_UPDATES, MINIMUM_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MINIMUM_TIME_BETWEEN_UPDATES, MINIMUM_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
    }

    public void stopUpdates() {
        if (locationListener != null) {
            locationManager.removeUpdates(locationListener);
            locationListener = null;
        }
    }


    public void update(){
        LocationManager locationManager = (LocationManager) mainActivity.getSystemService(mainActivity.LOCATION_SERVICE); //instantiated locationManager with the user's location information

        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mainActivity.showPhoneStatePermission();
            return;
        }

        setLocation(locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER)); //instantiate user based off phone's coordinates);
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
        if(location == null) return;
        this.location = location;
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        altitude = location.getAltitude();
    }


}
