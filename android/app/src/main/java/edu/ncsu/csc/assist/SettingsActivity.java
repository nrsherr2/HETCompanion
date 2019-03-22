package edu.ncsu.csc.assist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import edu.ncsu.csc.assist.data.sqlite.AppDatabase;
import edu.ncsu.csc.assist.data.sqlite.entities.ConfigOption;

import static edu.ncsu.csc.assist.DashboardActivity.EXTRAS_DEVICE_ADDRESS;
import static edu.ncsu.csc.assist.DashboardActivity.EXTRAS_DEVICE_NAME;

public class SettingsActivity extends AppCompatActivity {

    // Layout elements
    Button submitButton;
    EditText userId;
    EditText hetVersion;

    // Device info for dashboard
    private String mDeviceAddress;
    private String mDeviceName;

    AppDatabase db;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // set layout
        setContentView(R.layout.settings);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "ASSIST").build();

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
            // save in DB
            db.configOptionDao().insert(new ConfigOption("config_user_id", userIdStr));
            db.configOptionDao().insert(new ConfigOption("config_het_version", hetVersionStr));

            // Go to dashboard
            final Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra(DashboardActivity.EXTRAS_DEVICE_NAME, mDeviceName);
            intent.putExtra(DashboardActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
            startActivity(intent);
        } else {
            Toast.makeText(getBaseContext(), "Invalid Inputs. Must be Whole Numbers",
                    Toast.LENGTH_LONG).show();
        }
    }

    // Checks if the string passed in is an integer
    // returns integer rep of str, -1 if otherwise
    public int isValidUserID(String userIdStr) {
        try {
            return Integer.parseInt(userIdStr);
        } catch(NumberFormatException e) {
            return -1;
        }
    }

    // Checks if the string passed in is an integer
    // returns integer rep of str, -1 if otherwise
    public int isValidHetVersion(String hetVersionStr) {
        try {
            return Integer.parseInt(hetVersionStr);
        } catch(NumberFormatException e) {
            return -1;
        }
    }

}
