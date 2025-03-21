package se.ltu.navigator.location;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import androidx.core.app.ActivityCompat;

import se.ltu.navigator.MainActivity;
import se.ltu.navigator.navinfo.NavInfo;


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
    private final float MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 5;

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

    public double getAltitude() {return altitude;}

    public int getFloor() {return floor;}

    public Location getLocation() {
        //for debugging purposes use this
//        Location test = new Location("");
//        test.setLatitude(65.61768045184031);
//        test.setLongitude(22.138135688759508);
//        return test;
        return location;
    }

    /** Debugging version of getLocation()
     * @param debug Set to true of debug mode should be used
     *
     * @return A custom current position for debugging if debug flag is true - otherwise just return normal location
     */
    public  Location getLocation(boolean debug) {
        if (debug) {
            Location location = new Location("");
            location.setLatitude(65.61711132989254);
            location.setLongitude(22.137669155810737);
            return location;
        }
        else {
            return this.getLocation();
        }
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

        NavInfo.FLOOR.setData(Integer.toString(floor));
        mainActivity.watchBridge.setCurrentFloor(floor);

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

        Log.i(TAG, "Updating localisation");

        this.location = location;
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        altitude = location.getAltitude();
        mainActivity.onLocationChanged(longitude, latitude, altitude);

        mainActivity.watchBridge.setCurrentLocation(location);
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

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(mainActivity);

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, TIME_BETWEEN_UPDATES)
                .setMinUpdateDistanceMeters(MINIMUM_DISTANCE_CHANGE_FOR_UPDATES)
                .build();

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        setLocation(location); // Call your method with new location
                    }
                }
            }
        };

        // Request location updates
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainActivity.getMainLooper());


        // locationManager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, TIME_BETWEEN_UPDATES, MINIMUM_DISTANCE_CHANGE_FOR_UPDATES, this::setLocation);

        userSensorHandler.registerSensors();

        // Initializes lastPressure
        lastPressure = userSensorHandler.getPressure();

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
