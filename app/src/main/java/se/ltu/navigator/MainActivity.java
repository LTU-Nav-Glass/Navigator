package se.ltu.navigator;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import se.ltu.navigator.databinding.ActivityMainBinding;
import se.ltu.navigator.util.userLocationManager;

public class MainActivity extends AppCompatActivity {

    //private appPermissionRequest permissionRequest;

    private final int REQUEST_PERMISSION_FINE_LOCATION = 1;

    private userLocationManager user;
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

        initUser();

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

        // Logs tags of each number for testing
        Log.d(mainTag, "Altitude: " + user.getAltitude()); //test to see how accurate it is
        Log.d(mainTag, "Longitude: " + user.getLongitude());
        Log.d(mainTag, "Latitude: " + user.getLatitude());

    }


    public void initUser()
    {
        user = new userLocationManager(this);
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