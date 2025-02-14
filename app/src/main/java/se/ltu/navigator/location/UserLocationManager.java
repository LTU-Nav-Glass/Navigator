package se.ltu.navigator.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

import se.ltu.navigator.MainActivity;


public class UserLocationManager {
    private static final String TAG = "UserLocationManager";
    private final MainActivity mainActivity;

    private final LocationManager locationManager;
    private Location location;
    private double longitude, latitude, altitude;
    private int floor;

    private float lastPressure;

    private UserSensorManager userSensorManager;

    //these two variables will be used for updating user during movement
    private final long TIME_BETWEEN_UPDATES = 5000;
    private final float MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 0;

    public UserLocationManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        locationManager = (LocationManager) mainActivity.getSystemService(mainActivity.getApplicationContext().LOCATION_SERVICE); //instantiated locationManager with the user's location information
        userSensorManager = new UserSensorManager(this, mainActivity);
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public int getFloor() {
        return floor;
    }

    public Location getLocation() {
        return location;
    }

    public float getLastPressure(){ return lastPressure;}

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public void setFloor(int floor) {
        this.floor = floor;

        // resets pressure to new floor's pressure when changing floor
        userSensorManager.setLastPressure();
    }

    public void setLastPressure(float pressure)
    {
        lastPressure = pressure;
    }

    public void setLocation(Location location) {
        if (location == null) return;
        this.location = location;
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        altitude = location.getAltitude();

        Log.i(TAG, "Updating localisation");
    }

    /**
     * Starts location updates.
     */
    public void startUpdates() {
        // Check for permissions (location & sensors)
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            mainActivity.showPhoneStatePermission();
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, TIME_BETWEEN_UPDATES, MINIMUM_DISTANCE_CHANGE_FOR_UPDATES, this::setLocation);

        userSensorManager.registerSensors();

        // Initializes lastPressure
        lastPressure = userSensorManager.getPressure();
        Log.d(TAG, "Last Pressure: " + lastPressure);

    }

    /**
     * Stops location updates.
     */
    public void stopUpdates() {
        locationManager.removeUpdates(this::setLocation);
        userSensorManager.pauseSensors();
    }


    public void update() {
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mainActivity.showPhoneStatePermission();
            return;
        }

        setLocation(locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER)); //instantiate user based off phone's coordinates)
    }

}
