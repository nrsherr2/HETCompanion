package edu.ncsu.csc.assist;


import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import edu.ncsu.csc.assist.bluetooth.BluetoothLeService;

/**
 * Class that handles the main UI functionality and bluetooth connections
 */
public class DashboardActivity extends AppCompatActivity {
    //the current tab you're on
    private int currentTab;

    //the current fragment you're on
    private Fragment fragment;

    /* the constant names for device name and address */
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    /* the address of the device */
    private String mDeviceAddress;
    /* the service you're calling functions from */
    private BluetoothLeService bleService;
    /* the characteristic you're getting notifications of */
    private BluetoothGattCharacteristic notifyCharacteristic;
    /* UUIDs that represent the characteristics of the BLE device we're interested in */
    private final UUID fff0 = new UUID(0xfff000001000L, 0x800000805f9b34fbL);
    private final UUID fff3 = new UUID(0xfff300001000L, 0x800000805f9b34fbL);
    private final UUID fff1 = new UUID(0x0000fff100001000L, 0x800000805f9b34fbL);

    /**
     * Connection to the BLE service that ensures we're keeping information up-to-date
     */
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bleService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!bleService.initialize()) {
                System.out.println("Unable to initialize BLE service");
                finish();
            }
            bleService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bleService = null;
        }
    };

    /**
     * receiver that deals with updates sent by the BLE service
     */
    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                System.out.println("Dashboard received \"connected\"");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                System.out.println("Dashboard received \"disconnected\"");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                System.out.println("Services Discovered. Finding Characteristics...");
                listenForAttributes();
            } else if (BluetoothLeService.DATA_AVAILABLE.equals(action)) {
                //System.out.print("Data Available: ");
                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    /**
     * For now, just prints the data out. We can rename the method and make it change behaviors
     * when we integrate it into dev.
     *
     * @param data the information we want to display.
     */
    private void displayData(String data) {
        System.out.println(data);
    }

    /**
     * calls the necessary methods for enabling information streaming
     */
    private void listenForAttributes() {
        notifyCharacteristic = bleService.findAndSetNotify(fff0, fff3);
        if (notifyCharacteristic == null) {
            System.out.println("could not set up notifications.");
        }
    }

    /**
     * sets up stuff for navigation between screens
     */
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

    /**
     * steps to be taken when this class is created
     *
     * @param savedInstanceState the current environment of Android info
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("initiating dashboard");
        final Intent intent = getIntent();
        String mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * method that runs when the activity is switched to
     */
    @Override
    protected void onStart() {
        super.onStart();
        initiateDashboard();
    }

    /**
     * What happens when you go to a different activity and switch back to this one
     */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        if (bleService != null) {
            System.out.println("Connect request result: " + bleService.connect(mDeviceAddress));
        }
    }


    /**
     * methods that are called to ensure no memory leaks happen
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(gattUpdateReceiver);
        bleService.disconnect();
        unbindService(serviceConnection);
        bleService = null;
    }

    /**
     * sets up the dashboard view
     */
    private void initiateDashboard() {
        setContentView(R.layout.dashboard);
        BottomNavigationView navigation = findViewById(R.id.main_nav);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        fragment = new HomeFragment();
        loadFragment(fragment);
    }

    /**
     * logic for switching between tabs
     *
     * @param item the tabs selected
     * @return idk but it's the new view
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The ID of the menu item clicked
        int id = item.getItemId();
        if (id == R.id.action_status) {
            Intent intent = new Intent(this, StatusActivity.class);
            startActivity(intent);
            System.out.println("Switched to status activity");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * loads the fragment you want to display
     *
     * @param fragment the target fragment
     * @return the fragment after it's loaded
     */
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    fragment).commit();
            return true;
        }
        return false;
    }

    /**
     * Makes sure this activity receives broadcasts that it only cares about
     *
     * @return the filter that states which messages are important
     */
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.DATA_AVAILABLE);
        return intentFilter;
    }
}
