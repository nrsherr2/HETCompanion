package edu.ncsu.csc.assist;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import edu.ncsu.csc.assist.bluetooth.BtButtonActivity;

import static com.google.android.gms.auth.api.credentials.CredentialPickerConfig.Prompt.SIGN_IN;

/**
 * This is the main activity class. This activity runs every time the user starts the app after
 * closing it.
 * While this isn't really considered the "main" activity of this program (that belongs to the
 * dashboard), this activity makes sure that all of the important things needed for the
 * application to work are set up, such as permissions for bluetooth, and Google sign-in.
 */
public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    // int id of current tab id
    private int currentTab;

    // used for signing into the app
    private static final int SIGN_IN_CODE = 9001;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInAccount account;


    //device system for connecting to bluetooth devices, including BLE
    private BluetoothAdapter bluetoothAdapter;
    private static int REQUEST_ENABLE_BT = 6274;

    // used for enabling location services
    private static int REQUEST_ENABLE_GPS = 6275;


    // reference to current fragment if we need it
    private Fragment fragment;

    /**
     * This method works behind the scenes getting the application's data ready. The first thing
     * it does it prepare the Google sign-in by telling the app what information is needed, and
     * telling Google's servers about our signing in. After setting up the sign-in, it detects if
     * the device you are using supports BLE. If it does not, then you can't use the app.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the options sign in to be the default ones
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.server_client_id)).requestEmail().requestProfile().build();
        mGoogleApiClient =
                new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

        SignInClientHolder.setClient(mGoogleApiClient);

        // SignInButton signInButton = findViewById(R.id.sign_in_button);
        // signInButton.setSize(SignInButton.SIZE_STANDARD);


        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }


    }

    /**
     * this method runs whenever the activity is started. this function contains behaviors that
     * optimize working with the Google API to know exactly what to look for.
     */
    @Override
    protected void onStart() {
        setContentView(R.layout.signin);
        super.onStart();
        OptionalPendingResult<GoogleSignInResult> optPenRes =
                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (optPenRes.isDone()) {
            account = optPenRes.get().getSignInAccount();
            Log.i(getClass().getCanonicalName(), "Signed in as " + account.getDisplayName());
            Log.i(getClass().getCanonicalName(), "Google Client ID: " + account.getId());
        } else {
            optPenRes.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    account = googleSignInResult.getSignInAccount();
                    if (googleSignInResult.isSuccess()) {
                        Log.i(getClass().getCanonicalName(),
                                "Signed in as " + account.getDisplayName());
                        Log.i(getClass().getCanonicalName(),
                                "Google Client ID: " + account.getId());
                    } else {
                        Log.e(getClass().getCanonicalName(), "Error logging in: " +
                                googleSignInResult.getStatus().getStatusMessage());
                    }
                }
            });
        }
    }

    /**
     * This method is required somewhere, so we have to include it
     *
     * @param connectionResult the result of the connection
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        System.out.println("connection failed");
    }

    /**
     * The last method called in the starting of this app, this method makes sure your google
     * sign-in details work. If they do, then you are directed to the next page. Otherwise, you
     * are given the sign-in button.
     */
    @Override
    protected void onResume() {
        super.onResume();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            //initiateDashboard();
            System.out.println("initiate bluetooth from onResume");
            initiateBluetooth();
        } else {
            setContentView(R.layout.signin);
            SignInButton signInButton = findViewById(R.id.sign_in_button);
            signInButton.setOnClickListener(this);
        }
    }

    /**
     * if something on the screen is clicked, then this method is called
     *
     * @param v the object that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                //go to the google sign in
                signIn();
                break;
        }
    }

    /**
     * pulls up the google sign-in page that you see everywhere on Android.
     */
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, SIGN_IN_CODE);
    }


    /**
     * Asks for your permission to use GPS functionality. This is required for using BLE.
     * If you have already given permission, this is ignored.
     */
    private void initiateGPS() {
        //make sure location comes up first
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ENABLE_GPS);
        }
    }

    /**
     * Asks for your permission to use BLE. If you have already given permission, sends you to
     * the next part of the flow, BtButtonActivity.
     */
    private void initiateBluetooth() {
        initiateGPS();
        //now make sure bluetooth is running and that it is enabled.
        //will display a dialog box asking to run bluetooth
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            System.out.println("requestion bluetooth");
        }
        final Intent intent = new Intent(this, BtButtonActivity.class);
        startActivity(intent);
        //initiateDashboard();

    }


    /**
     * Deals with what happens when you ask for various functions. Mostly just cares about BLE
     * success and Google Sign-In.
     * Once you're signed in, you're directed to the BTButtonActivity.
     *
     * @param requestCode code [we made this code up] associated with each request.
     * @param resultCode  the result of that request
     * @param data        any additional data involved.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == SIGN_IN) {
            initiateBluetooth();
        }
        if (requestCode == REQUEST_ENABLE_BT) {
            //initiateDashboard();
            System.out.println("bluetooth ok");
        }

    }
}
