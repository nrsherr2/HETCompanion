package edu.ncsu.csc.assist;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    Button submitButton;
    EditText userId;
    EditText hetVersion;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // set layout
        setContentView(R.layout.settings);

        // save objects on screen as properties
        userId = findViewById(R.id.user_id);
        hetVersion = findViewById(R.id.het_version);
        submitButton = findViewById(R.id.settings_submit);

        // register submit button onClick listener
        submitButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
               setUserAttributes();
            }
        });

    }

    // Checks the inputs, saves them in the DB, and goes to the dashboard activity
    public void setUserAttributes() {
        String userIdStr = userId.getText().toString();
        String hetVersionStr = hetVersion.getText().toString();

        int userId = isValidUserID(userIdStr);
        int hetVersion = isValidHetVersion(hetVersionStr);

        if (userId != -1 && hetVersion != -1) {
            // TODO: Save in DB and go to dashboard
            Toast.makeText(getBaseContext(), "These are valid inputs. YAY!",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getBaseContext(), "Invalid Inputs. Must be Whole Numbers",
                    Toast.LENGTH_LONG).show();
        }
    }

    // Checks if the string passed in is an integer
    // returns -1 if otherwise
    // TODO: see if userID's are only integers and not alphanumeric
    public int isValidUserID(String userIdStr) {
        try {
            return Integer.parseInt(userIdStr);
        } catch(NumberFormatException e) {
            return -1;
        }
    }

    // Checks if the string passed in is an integer
    // returns -1 if otherwise
    // TODO: check format of HET version needed for storage and
    public int isValidHetVersion(String hetVersionStr) {
        try {
            return Integer.parseInt(hetVersionStr);
        } catch(NumberFormatException e) {
            return -1;
        }
    }

}
