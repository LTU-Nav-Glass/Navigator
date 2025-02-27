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
import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Marker;

/**
 * Compass logic manager responsible to change arrow and compass angle according to provided
 * locations - also updates information for mapView
 */
public class CompassManager implements SensorEventListener {
    public static final int SAMPLING_PERIOD_US = 20000;

    private final MainActivity mainActivity;
    private final SensorManager sensorManager;
    private final Sensor rotationSensor;

    // Data
    private NavigatorBridge.Room target;
    private final float[] rotationMatrix = new float[16];
    private final float[] orientationVector = new float[3];
    private float currentAzimuth;
    private float lastAzimuth;
    private float currentBearing;
    private float lastBearing;
    private int lastFloor;
    private int lastTargetFloor;
    private Marker targetMarker;

    public CompassManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        sensorManager = ((SensorManager) mainActivity.getSystemService(Context.SENSOR_SERVICE));
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        // Initialize the rotation matrix to identity
        rotationMatrix[ 0] = 1;
        rotationMatrix[ 4] = 1;
        rotationMatrix[ 8] = 1;
        rotationMatrix[12] = 1;

        lastFloor = Integer.MIN_VALUE;
        lastTargetFloor = Integer.MIN_VALUE;
    }

    private void updateFloorIcon() {
        if (this.target == null) {
            mainActivity.compassFloorIndicator.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            return;
        }

        int current = mainActivity.navigatorBridge.getCurrentFloor();
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
    }

    /**
     * Stops orientation monitoring.
     */
    public void stopMonitoring() {
        sensorManager.unregisterListener(this);
    }

    /**
     * @param target The new target room.
     */
    public void setTarget(@NotNull NavigatorBridge.Room target) {
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

            target = mainActivity.navigatorBridge.getTargetRoom();

            // Animate the rotation of the compass (disk & arrow)
            RotateAnimation rotateCompass = new RotateAnimation(-lastAzimuth, -currentAzimuth, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateCompass.setDuration(SAMPLING_PERIOD_US/1000);
            rotateCompass.setInterpolator(new LinearInterpolator());
            rotateCompass.setFillAfter(true);

            mainActivity.compass.startAnimation(rotateCompass);

            lastAzimuth = currentAzimuth;

            Location currentLocation = mainActivity.navigatorBridge.getCurrentLocation();
            if (currentLocation != null) {
                // centering the map layout on the newly detected location
                mainActivity.mapView.setCenter(new LatLong(currentLocation.getLatitude(), currentLocation.getLongitude()));

                if (mainActivity.navigatorBridge.getTargetRoom() != null) {
                    mainActivity.compassArrowText.setText(Math.round(currentLocation.distanceTo(target.getLocation())) + "m");

                    currentBearing = -currentLocation.bearingTo(target.getLocation());

                    // Animate the rotation of the compass arrow
                    RotateAnimation rotateArrow = new RotateAnimation(lastBearing, currentBearing, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    rotateArrow.setDuration(SAMPLING_PERIOD_US / 1000);
                    rotateArrow.setInterpolator(new LinearInterpolator());
                    rotateArrow.setFillAfter(true);

                    mainActivity.compassArrow.startAnimation(rotateArrow);

                    lastBearing = currentBearing;
                } else {
                    mainActivity.compassArrowText.setText("-");
                }
            } else {
                mainActivity.compassArrowText.setText("-");
            }

            if (mainActivity.navigatorBridge.getCurrentFloor() != lastFloor) {
                lastFloor = mainActivity.navigatorBridge.getCurrentFloor();
                mainActivity.compassFloorIndicator.setText(Integer.toString(lastFloor));
                updateFloorIcon();
            }

            if (target != null) {
                if (target.getFloor() != lastTargetFloor) {
                    updateFloorIcon();
                }
            } else {
                if (lastTargetFloor != Integer.MIN_VALUE) {
                    lastTargetFloor = Integer.MIN_VALUE;
                    updateFloorIcon();
                }
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

    }
}
