package se.ltu.navigator;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import se.ltu.navigator.databinding.ActivityMainBinding;
import se.ltu.navigator.locationAPI.LocationAPI;
import se.ltu.navigator.navinfo.NavInfoAdapter;
import se.ltu.navigator.search.SearchAdapter;
import se.ltu.navigator.util.FloorPromptHelper;
import se.ltu.navigator.util.UserLocationManager;

public class MainActivity extends AppCompatActivity {

    /*
     * View binding
     */

    private static final String TAG = "MainActivity"; //Tag usually indicates class log message comes from

    private final int REQUEST_PERMISSION_FINE_LOCATION = 1;

    private ActivityMainBinding binding;

    // Search
    protected SearchBar searchBar;
    protected SearchView searchView;
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

    /*
     * Logic
     */

    // Modules
    protected CompassManager compassManager;
    protected SearchBarManager searchBarManager;
    protected LocationAPI locationAPI;
    protected FloorPromptHelper floorPromptHelper;


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

        // Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        searchBar = findViewById(R.id.search_bar);
        searchView = findViewById(R.id.search_view);
        searchResults = findViewById(R.id.search_results);

        compass = findViewById(R.id.compass);
        compassDisk = findViewById(R.id.compass_disk);
        compassArrow = findViewById(R.id.compass_arrow);
        compassArrowText = findViewById(R.id.compass_arrow_text);

        bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        navInfo = findViewById(R.id.nav_info);

        // Initialize modules
        compassManager = new CompassManager(this);
        searchBarManager = new SearchBarManager(this);
        locationAPI = new LocationAPI(this);
        floorPromptHelper = new FloorPromptHelper(this, compassManager, "Your Floor", "What floor are you on?"); //when initialized, automattically prompts user for floor

        // Recycler views
        searchResults.setLayoutManager(new LinearLayoutManager(this));
        searchAdapter = new SearchAdapter(searchBarManager::search);
        searchResults.setAdapter(searchAdapter);

        navInfo.setLayoutManager(new LinearLayoutManager(this));
        navInfo.setAdapter(new NavInfoAdapter());

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
     * Method called when a key is typed in the search bar.
     *
     * @param v The view of the event.
     * @param keyCode The code of the key typed.
     * @param event The corresponding event.
     * @return True to consume the action, false otherwise.
     */
    private boolean onSearchKeyTyped(View v, int keyCode, KeyEvent event) {
        // TODO: Update the recycler view `searchResults` with the room list
        return false;
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
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     *
     * Controls what happens when the user agrees or disagrees to the permission prompt when opening the app
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
    public void promptUserFloor()
    {
        floorPromptHelper.showInputDialog();
    }

    /**
     *
     * @param title
     * @param message
     * @param permission
     * @param permissionRequestCode
     * This method shows the explanation of why the permissions are needed
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
     *
     * @param permissionName
     * @param permissionRequestCode
     * This method initially prompts the user for permissions
     */
    private void requestPermission(String permissionName, int permissionRequestCode)
    {
        ActivityCompat.requestPermissions(this, new String[] {permissionName}, permissionRequestCode);
    }

}