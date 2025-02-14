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
    private final float CHANGE_IN_FLOOR_PRESSURE = 10;

    private final MainActivity mainActivity;
    private UserLocationManager userLocationManager;
    private final SensorManager sensorManager;
    private Sensor barometer;
    private Sensor accelerometer;

    private float lastY;
    private float currentPressure;

    private boolean resetPressure = true;


    public UserSensorManager(UserLocationManager userLocationManager, MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
        this.userLocationManager = userLocationManager;

        lastY = 0;
        sensorManager = (SensorManager) mainActivity.getSystemService(mainActivity.getApplicationContext().SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        barometer = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    }

    public void registerSensors()
    {
        if (barometer != null) {
            //only registers sensorManager if accelerometer is present in the phone
            sensorManager.registerListener(sensorEventListener, barometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (accelerometer != null)
        {
            sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void pauseSensors()
    {
        sensorManager.unregisterListener(sensorEventListener);
    }

    public float getPressure()
    {
        Log.d(TAG, "currentPressure: " + currentPressure);
        return currentPressure;
    }

    public float getAcceleration()
    {
        return 0;
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
                currentPressure = event.values[0];

                if (resetPressure) //only sets LastPressure when its the first time getting to a floor
                {
                    userLocationManager.setLastPressure(currentPressure);
                    resetPressure = false;
                }

                Log.d("Barometer", "Floor " + userLocationManager.getFloor() + " pressure: " + currentPressure);

                if (detectPressureChange(event))
                {
                    mainActivity.promptUserFloor();
                    //if accurate --> userLocationManager.setFloor(+-1)
                }

            } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // Do work
                /**
                if(detectY(event))
                {
                    //may need to pause updates
                    //mainActivity.promptUserFloor();
                }
                 **/
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
     * @param e
     * @return
     */
    // This method (when accurate) can be adapted to return +-1 and increment/decrement user floor
    private boolean detectPressureChange(SensorEvent e)
    {

        //Measure difference b/w startingP & currentP
        float deltaP = userLocationManager.getLastPressure() - currentPressure;

        //Log.d(TAG, "Change in pressure: " + deltaP);

        return deltaP > CHANGE_IN_FLOOR_PRESSURE || deltaP < -(CHANGE_IN_FLOOR_PRESSURE); //estimating that 5 may be the right number
    }

    /**
     * This method updates the y coords of the user
     * @param e
     */
    // This method is for testing accuracy of acceleration, if barometer is accurate enough, then this method will not be needed
    private boolean detectY(SensorEvent e)
    {

        //CAUSES APP TO CRASH
        float y = e.values[2]; //Y-axis acceleration

        float deltaY = y - lastY;

        if(deltaY > 1.5)
        {

            Log.d(TAG, "Up");
            return true;
        } else if(deltaY < -1.5)
        {
            Log.d(TAG, "Down");
            return true;
        }
        lastY = y;

        return false;
    }
}
