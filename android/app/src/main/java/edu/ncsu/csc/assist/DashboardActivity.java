package edu.ncsu.csc.assist;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import edu.ncsu.csc.assist.bluetooth.BluetoothLeService;

public class DashboardActivity extends AppCompatActivity {
    private int currentTab;

    private Fragment fragment;

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private String mDeviceName, mDeviceAddress;
    private BluetoothLeService bleService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<>();
    private boolean connected = false;
    private BluetoothGattCharacteristic notifyCharacteristic;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private final UUID fff0 = new UUID(0xfff000001000L, 0x800000805f9b34fbL);
    private final UUID fff3 = new UUID(0xfff300001000L, 0x800000805f9b34fbL);
    private final UUID fff1 = new UUID(0x0000fff100001000L, 0x800000805f9b34fbL);

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

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                connected = true;
                System.out.println("Dashboard received \"connected\"");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                connected = false;
                System.out.println("Dashboard received \"disconnected\"");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                System.out.println("Services Discovered. Finding Characteristics...");
                listenForAttributes();
            } else if (BluetoothLeService.DATA_AVAILABLE.equals(action)) {
                System.out.print("Data Available: ");
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private void displayData(String data) {
        System.out.println(data);
    }

    private void listenForAttributes() {
        notifyCharacteristic = bleService.findAndSetNotify(fff0, fff3);
        if (notifyCharacteristic != null) {
            System.out.println(notifyCharacteristic.getUuid().toString() + " has been set to notify.");
        } else {
            System.out.println("could not set up notifications.");
        }
//        if (bleService.startStream(fff0, fff1)) {
//            System.out.println("Stream initialized");
//        }else{
//            System.out.println("did not start stream.");
//        }
    }

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
        System.out.println("initiating dashboard");
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initiateDashboard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        if (bleService != null) {
            System.out.println("Connect request result: " + bleService.connect(mDeviceAddress));
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(gattUpdateReceiver);
        bleService.disconnect();
        unbindService(serviceConnection);
        bleService = null;
    }

    private void initiateDashboard() {
        setContentView(R.layout.dashboard);
        BottomNavigationView navigation = findViewById(R.id.main_nav);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        fragment = new HomeFragment();
        loadFragment(fragment);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    fragment).commit();
            return true;
        }
        return false;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.DATA_AVAILABLE);
        return intentFilter;
    }
}
