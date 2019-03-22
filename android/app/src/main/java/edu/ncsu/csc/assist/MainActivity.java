package edu.ncsu.csc.assist;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import edu.ncsu.csc.assist.bluetooth.BtButtonActivity;

import static com.google.android.gms.auth.api.credentials.CredentialPickerConfig.Prompt.SIGN_IN;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    // int id of current tab id
    private int currentTab;

    private static final int SIGN_IN_CODE = 9001;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInAccount account;


    //device system for connecting to bluetooth devices, including BLE
    private BluetoothAdapter bluetoothAdapter;
    private static int REQUEST_ENABLE_BT = 6274;

    // used for enabling location services
    private static int REQUEST_ENABLE_GPS = 6275;

    private static int BLUETOOTH_BUTTON_CODE = 6273;

    // reference to current fragment if we need it
    private Fragment fragment;
    // on click listener for bottom nav
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            if (currentTab == item.getItemId()) {
                return true;
            }

            switch (item.getItemId()) {
                case R.id.home_tab:
                    System.out.println("HOME_TAB");
                    fragment = new HomeFragment();
                    break;
                case R.id.hr_tab:
                    System.out.println("HR_TAB");
                    fragment = new HeartRateFragment();
                    break;
                case R.id.hrv_tab:
                    System.out.println("HRV_TAB");
                    fragment = new HRVFragment();
                    break;
                case R.id.o3_tab:
                    System.out.println("o3_TAB");
                    fragment = new EnvFragment();
                    break;
            }
            currentTab = item.getItemId();
            return loadFragment(fragment);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the options sign in to be the default ones
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.server_client_id)).requestEmail().requestProfile().build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

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

    @Override
    protected void onStart() {
        setContentView(R.layout.signin);
        super.onStart();
        OptionalPendingResult<GoogleSignInResult> optPenRes = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
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
                        Log.i(getClass().getCanonicalName(), "Signed in as " + account.getDisplayName());
                        Log.i(getClass().getCanonicalName(), "Google Client ID: " + account.getId());
                    } else {
                        Log.e(getClass().getCanonicalName(), "Error logging in: " + googleSignInResult.getStatus().getStatusMessage());
                    }
                }
            });
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, SIGN_IN_CODE);
    }

    private void initiateDashboard() {
        setContentView(R.layout.dashboard);
        BottomNavigationView navigation = findViewById(R.id.main_nav);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        fragment = new HomeFragment();
        loadFragment(fragment);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

    }

    private void initiateGPS() {
        //make sure location comes up first
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ENABLE_GPS);
        }
    }

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

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    fragment).commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

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
