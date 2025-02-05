package se.ltu.navigator.util;

import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import se.ltu.navigator.CompassManager;
import se.ltu.navigator.MainActivity;

/*
 * Controls the pop-ups the user receives for flooring
 */
public class FloorPromptHelper {

    private MainActivity mainActivity;
    private CompassManager compassManager;
    private AlertDialog.Builder alert;
    private EditText input;
    private String title;
    private String enterComment;

    public FloorPromptHelper(MainActivity mainActivity, CompassManager compassManager, String title, String enterComment)
    {
        // Initiallize field vars
        this.mainActivity = mainActivity;
        this.compassManager = compassManager; //add method to access UserLocationManager object to change floor
        this.title = title;
        this.enterComment = enterComment;
    }

    public void showInputDialog()
    {
        initAlertDialog();
    }

    public void makeText(int button)
    {
        String userInput = input.getText().toString();

        if(button == 1 && userInput.matches("\\d+")) // checks that the ok button was pressed and input was a number
        {
            compassManager.getUserLocationManager().setFloor(Integer.valueOf(userInput));
            Toast.makeText(mainActivity, "Your floor is " + userInput, Toast.LENGTH_SHORT).show();
        } else
        {
            Toast.makeText(mainActivity, "Please enter a valid floor.", Toast.LENGTH_SHORT).show();
            showInputDialog();
        }
    }

    private void initAlertDialog()
    {
        alert = new AlertDialog.Builder(mainActivity);
        input = new EditText(mainActivity);

        alert.setTitle(this.title);
        alert.setMessage(this.enterComment);
        input.setHint("For ground level, put 0");
        alert.setView(input);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton)
            {
                String inputFloor = input.getText().toString().trim();
                if(inputFloor != null)
                {
                    makeText(1);
                } else {
                    makeText(0);
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alert.show();
    }
}
