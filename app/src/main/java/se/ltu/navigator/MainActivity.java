package se.ltu.navigator;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;

import se.ltu.navigator.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    /*
     * View binding
     */

    private static final String TAG = "MainActivity"; //Tag usually indicates class log message comes from
    private ActivityMainBinding binding;

    // Search
    protected SearchBar searchBar;
    protected SearchView searchView;
    protected RecyclerView searchResults;

    // Compass
    protected RelativeLayout compass;
    protected ImageView compassDisk;
    protected ImageView compassArrow;

    // Navigation infos
    protected LinearLayout bottomSheet;
    protected BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    protected RecyclerView navInfos;

    /*
     * Logic
     */

    // Compass logic
    private CompassManager compassManager;

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

        // Create compass manager
        compassManager = new CompassManager(this);
        this.compassManager.startMonitoring();

        // Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        searchBar = findViewById(R.id.search_bar);
        searchView = findViewById(R.id.search_view);
        searchResults = findViewById(R.id.search_results);

        compass = findViewById(R.id.compass);
        compassDisk = findViewById(R.id.compass_disk);
        compassArrow = findViewById(R.id.compass_arrow);

        bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // Event handling

        searchView.getEditText().setOnKeyListener(this::onSearchKeyTyped);
        searchView.getEditText().setOnEditorActionListener(this::onSearchAction);
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
}