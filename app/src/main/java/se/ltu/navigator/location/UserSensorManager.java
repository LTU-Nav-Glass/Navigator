package se.ltu.navigator.location;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import se.ltu.navigator.MainActivity;

public class UserSensorManager
{
    private static final String TAG = "UserSensorManager";
    private final float CHANGE_IN_FLOOR_PRESSURE = 0.34F;
    private final float PRESSURE_CHANGE_THRESHOLD = 0.04F;
   private final long FLOOR_CHANGE_TIMESTAMP = 3000;

    private final MainActivity mainActivity;
    private UserLocationManager userLocationManager;
    private final SensorManager sensorManager;
    private Sensor barometer;

    private int floorDirection = 0;
    private long lastTimestamp;
    private float[] currentPressures = new float[10];
    private int pIndex = 0;
    private float deltaP;
    private boolean resetPressure = true;


    public UserSensorManager(UserLocationManager userLocationManager, MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
        this.userLocationManager = userLocationManager;

        sensorManager = (SensorManager) mainActivity.getSystemService(mainActivity.getApplicationContext().SENSOR_SERVICE);
        barometer = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    }

    public void registerSensors()
    {
        if (barometer != null) {
            //only registers sensorManager if accelerometer is present in the phone
            sensorManager.registerListener(sensorEventListener, barometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void pauseSensors()
    {
        sensorManager.unregisterListener(sensorEventListener);
    }

    public float getPressure()
    {
        return currentPressures[pIndex];
    }

    /**
     * This method resets the reset boolean indicating that the lastPressure can be reset in the userLocationManager object
     */
    public void setLastPressure()
    {
        resetPressure = true;
    }

    /**
     * Listener that handles sensor events
     */
    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_PRESSURE) { //barometer
                // Do work
                currentPressures[pIndex] = event.values[0];
                if(pIndex == currentPressures.length - 1)
                    pIndex = 0;
                else
                    pIndex++;

                if (resetPressure) //only sets LastPressure when its the first time getting to a floor
                {
                    userLocationManager.setLastPressure(currentPressures[pIndex]);
                    Log.d("Barometer","set user pressure to " + currentPressures[pIndex]);

                    // reset conditional values
                    lastTimestamp = event.timestamp;
                    deltaP = 0;
                    resetPressure = false;
                }

                // Measure if the pressure changed and if the user has reached the target floor
                if (detectNewFloor(event))
                {
                    int userFloor = userLocationManager.getFloor();

                    userLocationManager.setLastPressure(currentPressures[pIndex]);
                    userLocationManager.setFloor(userFloor+floorDirection);

                    Log.d("Barometer","User floor is now: " + userLocationManager.getFloor());
                }

            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            if (sensor.getType() == Sensor.TYPE_PRESSURE)
            {
                Log.d(TAG, "baro acc: " + accuracy);
            } else if (sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            {
                Log.d(TAG, "accel acc: " + accuracy);
            }
        }

    };

    /**
     * This method takes in the sensor event (barometer) and measures if the difference b/w the lastPressure versus currentPressure to be enough for a floor change
     * @param e - current barometer sensor event
     * @return
     */
    // This method (when accurate) can be adapted to return +-1 and increment/decrement user floor
    private boolean detectPressureChange(SensorEvent e)
    {
        //Measure difference b/w startingP & currentP
        deltaP = userLocationManager.getLastPressure() - currentPressures[pIndex];

        //automatically change floor
        if(deltaP > CHANGE_IN_FLOOR_PRESSURE)
        {
            floorDirection = 1;
        }
        else if(deltaP < -CHANGE_IN_FLOOR_PRESSURE)
        {
            floorDirection = -1;
        }

        return Math.abs(deltaP) > CHANGE_IN_FLOOR_PRESSURE;
    }

    /**
     * This method returns true if the user has been on a new floor
     * @param e
     * @return
     */
    private boolean detectNewFloor(SensorEvent e)
    {
        //Measure if there is stability in currentPressure
        boolean pressureStable = true;
        for (int i = 1; i < currentPressures.length; i++)
        {
            if(Math.abs(currentPressures[i-1] - currentPressures[i]) > PRESSURE_CHANGE_THRESHOLD)
            {
                pressureStable = false;
                break;
            }
        }

        //Measure for pressure consistency on new floor
        long currentTimestamp = e.timestamp;
        long deltaTime = currentTimestamp - lastTimestamp;

        return detectPressureChange(e) &&
        ((pressureStable && deltaTime >= FLOOR_CHANGE_TIMESTAMP)
                || (mainActivity.getCompassManager().getTarget().getFloor() - userLocationManager.getFloor() > 1));
    }
}
