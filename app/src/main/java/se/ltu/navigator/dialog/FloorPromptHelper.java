package se.ltu.navigator.dialog;

import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import se.ltu.navigator.CompassManager;
import se.ltu.navigator.MainActivity;
import se.ltu.navigator.R;

// TODO: Add security to prevent two prompt to be displayed at the same time
// It will be useful when we add the floor change detection (which can trigger a prompt)

/*
 * Controls the pop-ups the user receives for flooring
 */
public class FloorPromptHelper extends DialogHelper {
    private EditText inputText;

    public FloorPromptHelper(MainActivity mainActivity, CompassManager compassManager) {
        super(mainActivity, compassManager);
    }

    @Override
    public void show() {
        MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(mainActivity);
        alert.setIcon(R.drawable.baseline_not_listed_location_24);
        alert.setTitle(R.string.title_floor_prompt);
        alert.setMessage(R.string.message_floor_prompt);
        alert.setView(R.layout.floor_prompt_view);

        alert.setPositiveButton(R.string.floor_prompt_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                int floor = Integer.parseInt(inputText.getText().toString());
                compassManager.getUserLocationManager().setFloor(floor);
                Snackbar.make(mainActivity.getRoot(), "Your floor is " + floor, Snackbar.LENGTH_SHORT)
                        .setAnchorView(R.id.bottom_sheet)
                        .show();
            }
        });

        alert.setNegativeButton(R.string.floor_prompt_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = alert.show();

        inputText = ((TextInputLayout) dialog.findViewById(R.id.floor_prompt_input)).getEditText();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

        // Add listener to enable the "Confirm" when we select a floor
        inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
