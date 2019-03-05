package edu.ncsu.csc.assist;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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

//    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
//    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
//    private String mDeviceName, mDeviceAddress;
//    private BluetoothLeService bleService;
//    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
//            new ArrayList<>();
//    private boolean connected = false;
//    private BluetoothGattCharacteristic notifyCharacteristic;
//    private final String LIST_NAME = "NAME";
//    private final String LIST_UUID = "UUID";
//    private final UUID fff3 = new UUID(0xfff300001000L, 0x800000805f9b34fbL);
//    private final UUID fff1 = new UUID(0x0000ffff100001000L, 0x800000805f9b34fbL);

//    private final ServiceConnection serviceConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            bleService = ((BluetoothLeService.LocalBinder) service).getService();
//            if (!bleService.initialize()) {
//                System.out.println("Unable to initialize BLE service");
//                finish();
//            }
//            bleService.connect(mDeviceAddress);
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            bleService = null;
//        }
//    };
//
//    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final String action = intent.getAction();
//            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
//                connected = true;
//                System.out.println("connected");
//            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
//                connected = false;
//                System.out.println("disconnected");
//            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
//                System.out.println("Services Discovered. Finding Characteristics...");
//                listenForAttributes();
//            } else if (BluetoothLeService.DATA_AVAILABLE.equals(action)) {
//                System.out.println("Data Available:");
//                displayData();
//            }
//        }
//    };
//
//    private void displayData() {
//    }
//
//    private void listenForAttributes() {
//        List<BluetoothGattService> gattServices = bleService.getSupportedGattServices();
//        if (gattServices == null) return;
//        String uuid = null;
//        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<>();
//        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<>();
//        mGattCharacteristics = new ArrayList<>();
//        for (BluetoothGattService bluetoothGattService : gattServices) {
//            HashMap<String, String> currentServiceData = new HashMap<>();
//            uuid = bluetoothGattService.getUuid().toString();
//            currentServiceData.put(LIST_NAME, "unknown");
//            currentServiceData.put(LIST_UUID, "uuid");
//            gattServiceData.add(currentServiceData);
//
//            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<>();
//            List<BluetoothGattCharacteristic> gattCharacteristics =
//                    bluetoothGattService.getCharacteristics();
//            ArrayList<BluetoothGattCharacteristic> characteristics = new ArrayList<>();
//
//            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
//                characteristics.add(gattCharacteristic);
//                HashMap<String, String> currentCharacteristicData = new HashMap<>();
//                uuid = gattCharacteristic.getUuid().toString();
//                currentCharacteristicData.put(LIST_NAME, "unknown");
//                currentCharacteristicData.put(LIST_UUID, "unknown");
//                gattCharacteristicGroupData.add(currentCharacteristicData);
//            }
//            mGattCharacteristics.add(characteristics);
//            gattCharacteristicData.add(gattCharacteristicGroupData);
//        }
//
//        if (mGattCharacteristics != null) {
//            //TODO iterate through known characteristics and find fff1
//            BluetoothGattCharacteristic fMega = null;
//            for (ArrayList<BluetoothGattCharacteristic> a : mGattCharacteristics) {
//                for (BluetoothGattCharacteristic g : a) {
//                    System.out.println(g.getUuid().toString());
//                    if (g.getUuid().toString().equals(fff1.toString())) {
//                        System.out.println("Properties of fff1: " + g.getProperties());
//                        if ((g.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) == 0) {
//                            System.out.println("fff1 does not have write property. Cannot use this device.");
//                        } else {
//                            fMega = g;
//                        }
//                    }
//                }
//            }
//            if (fMega == null) {
//                System.out.println("Could not find characteristic fff1. Can not read info.");
//                return;
//            }
//            //TODO find fff3 from list of characteristics and set a feed on it.
//
//            //TODO write '1' to fff1
//            if (fMega != null) {
//
//            }
//        }
//    }

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        initiateDashboard();
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
}
