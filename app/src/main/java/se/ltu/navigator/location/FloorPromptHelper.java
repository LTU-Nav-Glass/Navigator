package se.ltu.navigator.location;

import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import se.ltu.navigator.CompassManager;
import se.ltu.navigator.MainActivity;
import se.ltu.navigator.R;

/*
 * Controls the pop-ups the user receives for flooring
 */
public class FloorPromptHelper {

    private final MainActivity mainActivity;
    private final CompassManager compassManager;
    private EditText inputText;

    public FloorPromptHelper(MainActivity mainActivity, CompassManager compassManager)
    {
        // Initiallize field vars
        this.mainActivity = mainActivity;
        this.compassManager = compassManager; //add method to access UserLocationManager object to change floor
    }

    public void showInputDialog() {
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
                Toast.makeText(mainActivity, "Your floor is " + floor, Toast.LENGTH_SHORT).show();
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
