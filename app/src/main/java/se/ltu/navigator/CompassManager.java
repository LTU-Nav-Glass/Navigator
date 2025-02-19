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

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;
import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Marker;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Observer;

import se.ltu.navigator.location.Room;
import se.ltu.navigator.navinfo.NavInfo;
import se.ltu.navigator.location.UserLocationManager;

/**
 * Compass logic manager responsible to change arrow and compass angle according to provided
 * locations.
 */
public class CompassManager implements SensorEventListener {
    public static final int SAMPLING_PERIOD_US = 20000;

    private final MainActivity mainActivity;
    private final SensorManager sensorManager;
    private final Sensor rotationSensor;

    // Data
    private Room target;
    private final float[] rotationMatrix = new float[16];
    private final float[] orientationVector = new float[3];
    private float currentAzimuth;
    private float lastAzimuth;
    private float currentBearing;
    private float lastBearing;
    private final UserLocationManager userLocationManager;
    private Marker targetMarker;

    public CompassManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        userLocationManager = new UserLocationManager(mainActivity);

        sensorManager = ((SensorManager) mainActivity.getSystemService(Context.SENSOR_SERVICE));
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        // Initialize the rotation matrix to identity
        rotationMatrix[ 0] = 1;
        rotationMatrix[ 4] = 1;
        rotationMatrix[ 8] = 1;
        rotationMatrix[12] = 1;

        NavInfo.DISTANCE.registerListener((obs, arg) -> mainActivity.compassArrowText.setText((String) arg));

        NavInfo.FLOOR.registerListener((obs, arg) -> {
            mainActivity.compassFloorIndicator.setText((String) arg);

            if (Objects.equals(arg, "-") || this.target == null) {
                mainActivity.compassFloorIndicator.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            } else {
                updateFloorIcon();
            }
        });

        NavInfo.TARGET_FLOOR.registerListener((obs, arg) -> updateFloorIcon());
    }

    private void updateFloorIcon() {
        if (this.target == null) {
            mainActivity.compassFloorIndicator.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            return;
        }

        int current = this.userLocationManager.getFloor();
        int target = this.target.getFloor();

        if (current < target) {
            mainActivity.compassFloorIndicator.setCompoundDrawablesWithIntrinsicBounds(R.drawable.rounded_arrow_warm_up_24, 0, 0, 0);
        } else if (current > target) {
            mainActivity.compassFloorIndicator.setCompoundDrawablesWithIntrinsicBounds(R.drawable.rounded_arrow_cool_down_24, 0, 0, 0);
        } else {
            mainActivity.compassFloorIndicator.setCompoundDrawablesWithIntrinsicBounds(R.drawable.rounded_check_small_24, 0, 0, 0);
        }
    }

    /**
     * Starts orientation monitoring.
     */
    public void startMonitoring() {
        sensorManager.registerListener(this, rotationSensor, SAMPLING_PERIOD_US);
        userLocationManager.startUpdates();
    }

    /**
     * Stops orientation monitoring.
     */
    public void stopMonitoring() {
        sensorManager.unregisterListener(this);
        userLocationManager.stopUpdates();
    }

    /**
     * @param target The new target room.
     */
    public void setTarget(@NotNull Room target) {
        this.target = target;
        addTargetMarker(target.getLocation());
    }

    /**
     * Adds a marker to the mapView at the target location.
     * @param targetLocation The location to place the marker.
     */
    private void addTargetMarker(Location targetLocation) {
        if (targetMarker != null) {
            mainActivity.mapView.getLayerManager().getLayers().remove(targetMarker);
        }

        LatLong targetLatLong = new LatLong(targetLocation.getLatitude(), targetLocation.getLongitude());
        Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(mainActivity.getDrawable(R.drawable.marker_icon));
        targetMarker = new Marker(targetLatLong, bitmap, 0, 0);

        mainActivity.mapView.getLayerManager().getLayers().add(targetMarker);
    }

    /**
     * Returns the UserLocationManager object
     * @return
     */
    public UserLocationManager getUserLocationManager()
    {
        return this.userLocationManager;
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
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            // We compute the rotation matrix from the rotation vector
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            // We compute the orientation vector from the rotation matrix
            SensorManager.getOrientation(rotationMatrix, orientationVector);

            // Azimuth is [0]
            currentAzimuth = (float) Math.toDegrees(orientationVector[0]);
            NavInfo.AZIMUTH.setData(Math.round(currentAzimuth) + "°");

            // Animate the rotation of the compass (disk & arrow)
            RotateAnimation rotateCompass = new RotateAnimation(-lastAzimuth, -currentAzimuth, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateCompass.setDuration(SAMPLING_PERIOD_US/1000);
            rotateCompass.setInterpolator(new LinearInterpolator());
            rotateCompass.setFillAfter(true);

            mainActivity.compass.startAnimation(rotateCompass);

            lastAzimuth = currentAzimuth;

            Location currentLocation = userLocationManager.getLocation();
            if (currentLocation != null) {
                Instant instant = Instant.ofEpochMilli(currentLocation.getTime());
                NavInfo.LOCATION_ACCURACY.setData(Math.round(currentLocation.getAccuracy()) + "m");
                NavInfo.CURRENT_LOCATION.setData(currentLocation.getLatitude() + ", " + currentLocation.getLongitude() + "\n(" + Duration.between(instant, Instant.now()).toSeconds() + "s ago)");

                mainActivity.mapView.setCenter(new LatLong(currentLocation.getLatitude(), currentLocation.getLongitude()));

                if (target != null) {
                    NavInfo.DISTANCE.setData(Math.round(currentLocation.distanceTo(target.getLocation())) + "m");

                    currentBearing = currentLocation.bearingTo(target.getLocation());
                    NavInfo.BEARING.setData(Math.round(currentBearing) + "°");

                    // Animate the rotation of the compass (arrow)
                    RotateAnimation rotateArrow = new RotateAnimation(lastBearing, currentBearing, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    rotateArrow.setDuration(SAMPLING_PERIOD_US / 1000);
                    rotateArrow.setInterpolator(new LinearInterpolator());
                    rotateArrow.setFillAfter(true);

                    mainActivity.compassArrow.startAnimation(rotateArrow);

                    lastBearing = currentBearing;
                } else {
                    NavInfo.BEARING.setData("-");
                    NavInfo.DISTANCE.setData("-");
                }
            } else {
                NavInfo.CURRENT_LOCATION.setData("-");
            }

            if (target != null) {
                NavInfo.TARGET_LOCATION.setData(target.getLocation().getLatitude() + ", " + target.getLocation().getLongitude());
                NavInfo.TARGET_FLOOR.setData(Integer.toString(target.getFloor()));
            } else {
                NavInfo.TARGET_LOCATION.setData("-");
                NavInfo.TARGET_FLOOR.setData("-");
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
        switch (accuracy) {
            case SensorManager.SENSOR_STATUS_NO_CONTACT:
                warnAccuracy();
                NavInfo.COMPASS_ACCURACY.setData("NO CONTACT");
                break;
            case SensorManager.SENSOR_STATUS_UNRELIABLE:
                warnAccuracy();
                NavInfo.COMPASS_ACCURACY.setData("UNRELIABLE");
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                warnAccuracy();
                NavInfo.COMPASS_ACCURACY.setData("LOW");
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                NavInfo.COMPASS_ACCURACY.setData("MEDIUM");
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                NavInfo.COMPASS_ACCURACY.setData("HIGH");
                break;
        }
    }

    /**
     * Warn user about the compass accuracy.
     */
    private void warnAccuracy() {
        Snackbar.make(mainActivity.getRoot(), "The compass may be inaccurate or unavailable", Snackbar.LENGTH_INDEFINITE)
                .setAnchorView(R.id.bottom_sheet)
                .setAction("Got it", v -> {})
                .show();
    }
}
