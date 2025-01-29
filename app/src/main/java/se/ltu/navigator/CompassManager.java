package se.ltu.navigator;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

import org.jetbrains.annotations.NotNull;

import se.ltu.navigator.navinfo.NavInfo;
import se.ltu.navigator.util.UserLocationManager;

/**
 * Compass logic manager responsible to change arrow and compass angle according to provided
 * locations.
 */
public class CompassManager implements SensorEventListener {
    public static final int SAMPLING_PERIOD_US = 10000;

    private final MainActivity mainActivity;
    private final SensorManager sensorManager;
    private final Sensor rotationSensor;

    // Data
    private Location currentLocation;
    private Location targetLocation;
    private final float[] rotationMatrix = new float[16];
    private final float[] orientationVector = new float[3];
    private float currentAzimuth;
    private float lastAzimuth;
    private float currentBearing;
    private float lastBearing;
    private UserLocationManager userLocationManager;

    public CompassManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        sensorManager = ((SensorManager) mainActivity.getSystemService(Context.SENSOR_SERVICE));
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        // Initialize the rotation matrix to identity
        rotationMatrix[ 0] = 1;
        rotationMatrix[ 4] = 1;
        rotationMatrix[ 8] = 1;
        rotationMatrix[12] = 1;

        userLocationManager = new UserLocationManager(mainActivity);

        currentLocation = userLocationManager.getLocation();
    }

    /**
     * Starts orientation monitoring.
     */
    public void startMonitoring() {
        sensorManager.registerListener(this, rotationSensor, SAMPLING_PERIOD_US);
    }

    /**
     * Stops orientation monitoring.
     */
    public void stopMonitoring() {
        sensorManager.unregisterListener(this);
    }

    /**
     * @param currentLocation The current location of the device.
     */
    public void setCurrentLocation(@NotNull Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    /**
     * @param targetLocation The current location of the target.
     */
    public void setTargetLocation(@NotNull Location targetLocation) {
        this.targetLocation = targetLocation;
    }

    /**
     * Called when there is a new sensor event.  Note that "on changed"
     * is somewhat of a misnomer, as this will also be called if we have a
     * new reading from a sensor with the exact same sensor values (but a
     * newer timestamp).
     *
     * <p>See {@link SensorManager SensorManager}
     * for details on possible sensor types.
     * <p>See also {@link SensorEvent SensorEvent}.
     *
     * <p><b>NOTE:</b> The application doesn't own the
     * {@link SensorEvent event}
     * object passed as a parameter and therefore cannot hold on to it.
     * The object may be part of an internal pool and may be reused by
     * the framework.
     *
     * @param event the {@link SensorEvent SensorEvent}.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        userLocationManager.update();
        this.currentLocation = userLocationManager.getLocation();

        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            // We compute the rotation matrix from the rotation vector
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            // We compute the orientation vector from the rotation matrix
            SensorManager.getOrientation(rotationMatrix, orientationVector);

            // Azimuth is [0]
            currentAzimuth = (float) Math.toDegrees(orientationVector[0]);
            NavInfo.AZIMUTH.setData(currentAzimuth + "°");

            // Animate the rotation of the compass (disk & arrow)
            RotateAnimation rotateCompass = new RotateAnimation(-lastAzimuth, -currentAzimuth, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateCompass.setDuration(SAMPLING_PERIOD_US/1000);
            rotateCompass.setInterpolator(new LinearInterpolator());
            rotateCompass.setFillAfter(true);

            mainActivity.compass.startAnimation(rotateCompass);

            lastAzimuth = currentAzimuth;

            if (currentLocation != null && targetLocation != null) {
                NavInfo.CURRENT_LOCATION.setData(currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
                NavInfo.CURRENT_LOCATION.setData(targetLocation.getLatitude() + ", " + targetLocation.getLongitude());
                NavInfo.DISTANCE.setData(currentLocation.distanceTo(targetLocation) + "m");

                currentBearing = currentLocation.bearingTo(targetLocation);
                NavInfo.BEARING.setData(currentBearing + "°");

                // Animate the rotation of the compass (arrow)
                RotateAnimation rotateArrow = new RotateAnimation(-lastBearing, -currentBearing, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotateArrow.setDuration(SAMPLING_PERIOD_US/1000);
                rotateArrow.setInterpolator(new LinearInterpolator());
                rotateArrow.setFillAfter(true);

                mainActivity.compassArrow.startAnimation(rotateArrow);

                lastBearing = currentBearing;
            }
        }
    }

    /**
     * Called when the accuracy of the registered sensor has changed.  Unlike
     * onSensorChanged(), this is only called when this accuracy value changes.
     *
     * <p>See the SENSOR_STATUS_* constants in
     * {@link SensorManager SensorManager} for details.
     *
     * @param sensor
     * @param accuracy The new accuracy of this sensor, one of
     *                 {@code SensorManager.SENSOR_STATUS_*}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Ignore?
    }
}
