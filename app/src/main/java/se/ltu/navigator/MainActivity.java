package se.ltu.navigator;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import se.ltu.navigator.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private final int REQUEST_PERMISSION_FINE_LOCATION = 1;

    //these two variables will be used for updating user during movement
    private final long MINIMUM_TIME_BETWEEN_UPDATES = 55;
    private final float MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 3;

    private Context context; //provides info regarding the user's location

    private Location user; //location object storing the user's lat and long
    private LocationManager locationManager;
    private LocationListener locationListener; //able to give current updates to locationManager

    private double user_alt,user_long,user_lat; //private vars holding user's altitude, longitude, and latitude

    private ActivityMainBinding binding;

    private static final String mainTag = "MainActivity"; //Tag usually indicates method log message comes from

    /**
     *
      * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     * This class controls how the app is created whenever the user starts up the app. UI creation and variable instantiation is housed here
     *  ~ The main conditional measures if permissions are enabled and only instantiate the user and locationManager if they are.
     *  ~ The locationManager controls all the information that the Location object user has to get the current longitude and latitude
     *  ~ The log statements are for testing how the user is instantiated and if the info is correct.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initUI();

        Log.d(mainTag, "Created"); //test to see if log cat works


        //Conditional to measure if perms have been granted
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(mainTag, "No Permission");

            showPhoneStatePermission();
            return;
        } else {

            locationManager = (LocationManager) getSystemService(context.LOCATION_SERVICE); //instantiated locationManager with the user's location information

            Log.d(mainTag, "Permission");

            user = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER); //instantiate user based off phone's coordinates

            //Line below may be used for getting location updates
            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MINIMUM_TIME_BETWEEN_UPDATES, MINIMUM_DISTANCE_CHANGE_FOR_UPDATES, locationListener);

        }

    }

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

        user = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER); //instantiate user based off phone's coordinates

        // Puts data from Location object into field variables
        user_alt = user.getAltitude();
        user_long = user.getLongitude();
        user_lat = user.getLatitude();

        // Logs tags of each number for testing
        Log.d(mainTag, "Altitude: " + user_alt); //test to see how accurate it is
        Log.d(mainTag, "Longitude: " + user_long);
        Log.d(mainTag, "Latitude: " + user_lat);

    }

    /**
     * This method initializes the basic UI of the preset of the app
     */
    private void initUI() {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
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
    private void showPhoneStatePermission()
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