package se.ltu.navigator;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.rendertheme.ExternalRenderTheme;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;

import se.ltu.navigator.databinding.ActivityMainBinding;
import se.ltu.navigator.dialog.FloorPromptHelper;
import se.ltu.navigator.fingerprint.FingerprintManager;
import se.ltu.navigator.location.LocationAPI;
import se.ltu.navigator.navinfo.NavInfoAdapter;
import se.ltu.navigator.search.SearchAdapter;

public class MainActivity extends AppCompatActivity {

    /*
     * View binding
     */

    private static final String TAG = "MainActivity"; //Tag usually indicates class log message comes from

    private final int REQUEST_PERMISSION_FINE_LOCATION = 1;

    private ActivityMainBinding binding;

    // Search
    protected AppBarLayout appBar;
    protected SearchBar searchBar;
    protected SearchView searchView;
    protected LinearProgressIndicator searchProgress;
    protected RecyclerView searchResults;
    protected SearchAdapter searchAdapter;

    // Compass
    protected RelativeLayout compass;
    protected ImageView compassDisk;
    protected RelativeLayout compassArrow;
    protected TextView compassArrowText;

    // Navigation infos
    protected LinearLayout bottomSheet;
    protected BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    protected RecyclerView navInfo;

    // Map Stuff
    protected ViewSwitcher mapSwitcher;
    protected Button mapButton;

    protected MapView mapView;

    /*
     * Logic
     */

    // Modules
    protected CompassManager compassManager;
    protected SearchBarManager searchBarManager;
    protected LocationAPI locationAPI;
    protected FloorPromptHelper floorPromptHelper;
    protected FingerprintManager fingerprintManager;

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

        appBar = findViewById(R.id.app_bar);
        searchBar = findViewById(R.id.search_bar);
        searchView = findViewById(R.id.search_view);
        searchProgress = findViewById(R.id.search_progress);
        searchResults = findViewById(R.id.search_results);

        compass = findViewById(R.id.compass);
        compassDisk = findViewById(R.id.compass_disk);
        compassArrow = findViewById(R.id.compass_arrow);
        compassArrowText = findViewById(R.id.compass_arrow_text);

        bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        navInfo = findViewById(R.id.nav_info);

        //Map Stuff
        mapSwitcher = findViewById(R.id.map_switcher);
        mapButton = findViewById(R.id.map_button);

        mapView = findViewById(R.id.mapView);
        mapView.setClickable(false);
        mapView.getMapScaleBar().setVisible(false);
        mapView.setBuiltInZoomControls(false);
        mapSetup();

        // Initialize modules
        compassManager = new CompassManager(this);
        searchBarManager = new SearchBarManager(this);
        locationAPI = new LocationAPI(this);
        fingerprintManager = new FingerprintManager(this);
        floorPromptHelper = new FloorPromptHelper(this, compassManager); //when initialized, automattically prompts user for floor

        // Recycler views
        searchResults.setLayoutManager(new LinearLayoutManager(this));
        searchAdapter = new SearchAdapter(searchBarManager::search);
        searchResults.setAdapter(searchAdapter);

        navInfo.setLayoutManager(new LinearLayoutManager(this));
        navInfo.setAdapter(new NavInfoAdapter());

        // Disable "Drag" for AppBarLayout
        // https://stackoverflow.com/questions/34108501/how-to-disable-scrolling-of-appbarlayout-in-coordinatorlayout
        if (appBar.getLayoutParams() != null) {
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) appBar.getLayoutParams();
            AppBarLayout.Behavior appBarLayoutBehaviour = new AppBarLayout.Behavior();
            appBarLayoutBehaviour.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
                @Override
                public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                    return false;
                }
            });
            layoutParams.setBehavior(appBarLayoutBehaviour);
        }

        mapButton.setOnClickListener(v -> {
            mapSwitcher.showNext();
        });
    }

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
    }

    /**
     * Method called when the activity loses focus.
     */
    @Override
    protected void onPause() {
        super.onPause();

        this.compassManager.stopMonitoring();
    }

    /**
     * This method measures when the sensor changes
     * This is used in determining when to prompt user for input of floor
     * @param e
     */
    /**
    public void onSensorChange(SensorEvent e)
    {

        //update z param of userLocationManager
        if(e.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {

            if(compassManager.getUserLocationManager().detectZ(e))
            {
                promptUserFloor();
            }

        }

    }
     NOT CURRENTLY IMPLEMENTED
    **/



    /**
     * This method instantiates the field variables of longitude, latitude, and the user's altitude
     *  ~ Currently the method sets user to the last known location but that may change when wanting to update in real time
     */
    @Override
    public void onStart()
    {
        super.onStart();

        //Checks if permissions were enabled
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showPhoneStatePermission();
            return;
        }
    }

    /**
     * Method called when an action (other than typing) is performed on the search bar.
     *
     * @param v The view of the event.
     * @param actionID The code of the action.
     * @param event The corresponding event.
     * @return True to consume the action, false otherwise.
     */
    private boolean onSearchAction(View v, int actionID, KeyEvent event) {
        // TODO: Verify the name of the room before setting text
        searchBar.setText(searchView.getText());
        searchView.hide();
        return false;
    }

    /**
     * Controls what happens when the user agrees or disagrees to the permission prompt when opening the app
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case REQUEST_PERMISSION_FINE_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(MainActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                } else
                {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    /**
     * This method is run when the app is created and shows the current phone state and displays what permissions are needed
     */
    public void showPhoneStatePermission()
    {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION);
        //Conditional to check if perms are granted
        if (permissionCheck != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_FINE_LOCATION))
            {
                showExplanation("Permission Needed", "Rationale", Manifest.permission.READ_PHONE_STATE, REQUEST_PERMISSION_FINE_LOCATION);
            } else
            {
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_FINE_LOCATION);
            }
        } else {
            Toast.makeText(MainActivity.this, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Handles calls of FloorPromptHelper Object
     */
    public void promptUserFloor() {
        floorPromptHelper.show();
    }

    /**
     * This method shows the explanation of why the permissions are needed
     *
     * @param title
     * @param message
     * @param permission
     * @param permissionRequestCode
     */
    private void showExplanation(String title, String message, final String permission, final int permissionRequestCode)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(title).setMessage(message).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermission(permission, permissionRequestCode);
            }
        });

        builder.create().show();
    }

    /**
     * This method initially prompts the user for permissions
     *
     * @param permissionName
     * @param permissionRequestCode
     */
    private void requestPermission(String permissionName, int permissionRequestCode)
    {
        ActivityCompat.requestPermissions(this, new String[] {permissionName}, permissionRequestCode);
    }

    public View getRoot() {
        return binding.getRoot();
    }
}