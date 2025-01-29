package se.ltu.navigator;

import android.location.Location;
import android.view.KeyEvent;
import android.view.View;

public class SearchBarManager {
    private MainActivity mainActivity;

    public SearchBarManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        mainActivity.searchView.getEditText().setOnKeyListener(this::onSearchKeyTyped);
        mainActivity.searchView.getEditText().setOnEditorActionListener(this::onSearchAction);
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
        mainActivity.searchAdapter.setResults(mainActivity.locationAPI.findLocationsByPartialId(mainActivity.searchView.getText().toString()));
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
        Location location = mainActivity.locationAPI.getLocationById(mainActivity.searchView.getText().toString());

        if (location != null) {
            mainActivity.searchBar.setText(mainActivity.searchView.getText());
            mainActivity.searchView.hide();
            mainActivity.compassManager.setTargetLocation(location);
        }
        return false;
    }
}
