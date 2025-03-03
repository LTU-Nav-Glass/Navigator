package se.ltu.navigator;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.wearable.Wearable;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.ExternalRenderTheme;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import se.ltu.navigator.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    /*
     * View binding
     */

    private ActivityMainBinding binding;

    // Compass
    protected ViewSwitcher compass;
    protected ImageView compassDisk;
    protected RelativeLayout compassArrow;
    protected TextView compassArrowText;
    protected LinearLayout compassFloorIndicatorWrapper;
    protected TextView compassFloorIndicator;
    protected MapView mapView;

    /*
     * Logic
     */

    // Modules
    protected NavigatorBridge navigatorBridge;
    protected CompassManager compassManager;
    protected MapManager mapManager;

    /**
     * Method called when the view is created.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidGraphicFactory.createInstance(getApplication());

        // Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        compass = findViewById(R.id.compass);
        compassDisk = findViewById(R.id.compass_disk);
        compassArrow = findViewById(R.id.compass_arrow);
        compassArrowText = findViewById(R.id.compass_arrow_text);
        compassFloorIndicatorWrapper = findViewById(R.id.floor_indicator_wrapper);
        compassFloorIndicator = findViewById(R.id.floor_indicator);

        mapView = findViewById(R.id.mapView);
        mapView.setClickable(false);
        mapView.getMapScaleBar().setVisible(false);
        mapView.setBuiltInZoomControls(false);
        mapSetup();

        // Initialize modules
        navigatorBridge = new NavigatorBridge();
        compassManager = new CompassManager(this);
        mapManager = new MapManager(this, compassManager);
    }

    // setting up the initial mapView -> further logic takes place in CompassManager
    @SuppressLint("ClickableViewAccessibility")
    private void mapSetup() {
        try {
            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open("planet_22.13,65.615_22.151,65.621.map");
            File tempFile = File.createTempFile("temp_map", ".map", getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            TileCache tileCache = AndroidUtil.createTileCache(this, "mapcache",
                    mapView.getModel().displayModel.getTileSize(), 1f,
                    mapView.getModel().frameBufferModel.getOverdrawFactor());

            MapFile mapData = new MapFile(tempFile);

            TileRendererLayer tileRendererLayer = new TileRendererLayer(tileCache,
                    mapData, mapView.getModel().mapViewPosition,
                    AndroidGraphicFactory.INSTANCE);

            File renderThemeFile = new File(getCacheDir(), "default.xml");
            try (InputStream renderThemeStream = assetManager.open("default.xml");
                 FileOutputStream renderThemeOutputStream = new FileOutputStream(renderThemeFile)) {
                while ((length = renderThemeStream.read(buffer)) > 0) {
                    renderThemeOutputStream.write(buffer, 0, length);
                }
            }

            tileRendererLayer.setXmlRenderTheme(new ExternalRenderTheme(renderThemeFile));


            mapView.getLayerManager().getLayers().add(tileRendererLayer);
            mapView.setCenter(new LatLong(65.618, 22.141));
            mapView.setZoomLevel((byte) 18);

            // Detecting double tap
            final GestureDetector detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onDown(@NonNull MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onDoubleTap(@NonNull MotionEvent e) {
                    compass.showNext();
                    return true;
                }
            });

            compass.setOnTouchListener((v, event) -> detector.onTouchEvent(event));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method called when the activity gains focus.
     */
    @Override
    protected void onResume() {
        super.onResume();

        this.compassManager.startMonitoring();
        Wearable.getDataClient(this).addListener(navigatorBridge);
    }

    /**
     * Method called when the activity loses focus.
     */
    @Override
    protected void onPause() {
        super.onPause();

        this.compassManager.stopMonitoring();
        Wearable.getDataClient(this).removeListener(navigatorBridge);
    }
}
