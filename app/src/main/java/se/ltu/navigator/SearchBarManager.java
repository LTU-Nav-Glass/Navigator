package se.ltu.navigator;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;

public class SearchBarManager implements TextWatcher {
    private MainActivity mainActivity;

    public SearchBarManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        mainActivity.searchView.getEditText().addTextChangedListener(this);
        mainActivity.searchView.getEditText().setOnEditorActionListener(this::onSearchAction);
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
        if (event != null) this.search(mainActivity.searchView.getText().toString());
        return false;
    }

    /**
     * Close the search bar if the search location is valid.
     *
     * @param search The location name.
     */
    public void search(String search) {
        mainActivity.searchProgress.setVisibility(View.VISIBLE);
        mainActivity.searchProgress.setIndeterminate(true);
        mainActivity.locationAPI.getRoomById(search, room -> {
            if (room != null) {
                mainActivity.runOnUiThread(() -> {
                    mainActivity.searchBar.setText(search);
                    mainActivity.searchView.hide();
                    mainActivity.compassManager.setTarget(room);
                    mainActivity.promptUserFloor();
                });
            }
            mainActivity.runOnUiThread(() -> {
                mainActivity.searchProgress.setIndeterminate(false);
                mainActivity.searchProgress.setVisibility(View.GONE);
            });
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mainActivity.searchAdapter.setResults(mainActivity.locationAPI.findLocationsByPartialId(mainActivity.searchView.getText().toString()));
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
