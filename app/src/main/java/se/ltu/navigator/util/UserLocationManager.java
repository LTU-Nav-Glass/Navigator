package se.ltu.navigator.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import se.ltu.navigator.MainActivity;


public class UserLocationManager
{
    private static final String TAG = "UserLocationManager";
    private final MainActivity mainActivity;

    private final SensorManager sensorManager;

    private final Sensor accelerometer;

    private float lastZ;

    private final LocationManager locationManager;
    private Location location;
    private double longitude, latitude, altitude;
    private int floor;

    //these two variables will be used for updating user during movement
    private final long TIME_BETWEEN_UPDATES = 5000;
    private final float MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 0;

    public UserLocationManager(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
        locationManager = (LocationManager) mainActivity.getSystemService(Context.LOCATION_SERVICE); //instantiated locationManager with the user's location information

        //Instantiating vars for vertical movement detection

        lastZ = 0;
        sensorManager = (SensorManager) mainActivity.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
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

    public int getFloor()
    {
        return floor;
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

        if (accelerometer != null)
        {
            //only registers sensorManager if accelerometer is present in the phone
            //sensorManager.registerListener((SensorEventListener) mainActivity, accelerometer, SensorManager.SENSOR_DELAY_UI);
            Log.d(TAG, "Runs registerListener");
        }

    }

    /**
     * Stops location updates.
     */
    public void stopUpdates() {
        locationManager.removeUpdates(this::setLocation);
        //sensorManager.unregisterListener((SensorListener) mainActivity);
    }


    public void update(){
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mainActivity.showPhoneStatePermission();
            return;
        }

        setLocation(locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER)); //instantiate user based off phone's coordinates)
    }

    /**
     * This method updates the z coords of the user
     * @param e
     */
    /**
    public boolean detectZ(SensorEvent e)
    {
        float z = e.values[2]; //Z-axis acceleration

        float deltaZ = z - lastZ;

        if(deltaZ > 1.5) //Should indicate upwards movement
            {
                Log.d(TAG, "Up");
                return true;
            } else if(deltaZ < -1.5) //Should indicate downwards movement
            {
                Log.d(TAG, "Down");
                return true;
            }
        lastZ = z;
        return false;
    }
     NOT CURRENTLY IMPLEMENTED
     **/

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

    public void setFloor(int floor)
    {
        this.floor = floor;
        Log.d(TAG, "Floor set to: " + this.floor);
    }

    public void setLocation(Location location) {
        if(location == null) return;
        this.location = location;
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        altitude = location.getAltitude();

        Log.i("LOC", "Localisation");
    }
}
