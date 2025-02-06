package se.ltu.navigator.dialog;

import android.widget.EditText;

import se.ltu.navigator.CompassManager;
import se.ltu.navigator.MainActivity;

/**
 * Abstract class to describe dialog helpers
 */
public abstract class DialogHelper {
    protected final MainActivity mainActivity;
    protected final CompassManager compassManager;

    public DialogHelper(MainActivity mainActivity, CompassManager compassManager) {
        // Initialize field vars
        this.mainActivity = mainActivity;
        this.compassManager = compassManager; //add method to access UserLocationManager object to change floor
    }

    /**
     * Show the dialog.
     */
    public abstract void show();
}
