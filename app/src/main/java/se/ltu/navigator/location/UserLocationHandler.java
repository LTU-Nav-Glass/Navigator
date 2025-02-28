package se.ltu.navigator.location;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import se.ltu.navigator.MainActivity;


public class UserLocationHandler {
    private static final String TAG = "UserLocationHandler";
    private final MainActivity mainActivity;

    private final LocationManager locationManager;
    private Location location;
    private Location targetLocation;
    private double longitude, latitude, altitude;
    private int floor;

    private float lastPressure;

    private UserSensorHandler userSensorHandler;

    //these two variables will be used for updating user during movement
    private final long TIME_BETWEEN_UPDATES = 5000;
    private final float MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 0;

    public UserLocationHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        locationManager = (LocationManager) mainActivity.getSystemService(mainActivity.getApplicationContext().LOCATION_SERVICE); //instantiated locationManager with the user's location information
        userSensorHandler = new UserSensorHandler(this, mainActivity);
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
        //for debugging purposes use this
//        Location test = new Location("");
//        test.setLatitude(65.61768045184031);
//        test.setLongitude(22.138135688759508);
//        return test;
        return location;
    }

    public float getLastPressure(){ return lastPressure;}

    public Location getTargetLocation(){return targetLocation;}

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
        userSensorHandler.allowLastPressureReset();
    }

    public void setTargetLocation(Location targetLocation) {this.targetLocation = targetLocation;}


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
        mainActivity.onLocationChanged(longitude, latitude, altitude);

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

        userSensorHandler.registerSensors();

        // Initializes lastPressure
        lastPressure = userSensorHandler.getPressure();
        Log.d(TAG, "Last Pressure: " + lastPressure);

    }

    /**
     * Stops location updates.
     */
    public void stopUpdates() {
        locationManager.removeUpdates(this::setLocation);
        userSensorHandler.pauseSensors();
    }


    public void update() {
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mainActivity.showPhoneStatePermission();
            return;
        }

        //instantiate user based off phone's coordinates
        setLocation(locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER));
    }

}
