package edu.ncsu.csc.assist.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import edu.ncsu.csc.assist.DashboardActivity;
import edu.ncsu.csc.assist.R;
import edu.ncsu.csc.assist.SettingsActivity;

/**
 * This activity deals with searching for BLE devices and selecting one to connect to. Most of
 * the BLE logic comes from DeviceScanActivity in the Android sample project. That project
 * doesn't let you connect two devices, though, so I played around with this class to get a
 * better looking UI.
 * On this page, you press the "scan now" button to scan for devices. After that, the button
 * disappears and you will see a list of checkable devices. you can press "refresh" to refresh
 * the device list, and you can press "continue" to continue. The screen won't let you continue
 * if you don't have 1 or 2 devices selected.
 */
public class BtButtonActivity extends Activity {
    private Handler handler;
    private BluetoothAdapter bluetoothAdapter;
    private ListView devicesListView;
    private ArrayAdapter<BluetoothDeviceInfoWrapper> deviceListAdapter;
    private boolean scanning;

    /**
     * This method is called any time another activity starts this activity. Changes the page to
     * the bluetooth_connect page, then makes sure everything is okay for bluetooth scanning.
     *
     * @param savedInstanceState any variables floating around in the environment.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //change the page
        setContentView(R.layout.bluetooth_connect);
        handler = new Handler();

        //make sure bluetooth works again
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.bl_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // find the list that we are displaying teh devices in
        devicesListView = (ListView) findViewById(R.id.gatt_devices_list);
        //set up a list of devices
        ArrayList<BluetoothDeviceInfoWrapper> deviceList;
        deviceList = new ArrayList<>();
        // create an adapter that translates data between the device list and the view displaying
        // the list
        deviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, deviceList);
        devicesListView.setAdapter(deviceListAdapter);
    }

    /**
     * This method is called when you go back into the app on this screen. Makes sure you have
     * permissions.
     */
    @Override
    protected void onResume() {

        super.onResume();
        //make sure location is still allowed
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ENABLE_GPS);
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    /**
     * Whenever you pause the app on this screen, it resets the screen, basically.
     */
    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        deviceListAdapter.clear();
        findViewById(R.id.btScanButton).setVisibility(View.VISIBLE);
        findViewById(R.id.btRefresh).setVisibility(View.INVISIBLE);
        findViewById(R.id.btDone).setVisibility(View.INVISIBLE);
    }

    /**
     * Scans the environment for bluetooth low energy devices. This runs for about 10 seconds.
     *
     * @param enable whether we are starting or stopping the scan
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            //this thread will run in 10 seconds, it stops scanning
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    bluetoothAdapter.stopLeScan(leScanCallback);
                }
            }, SCAN_PERIOD);
            //in the meantime, start scanning
            scanning = true;
            bluetoothAdapter.startLeScan(leScanCallback);
        } else {
            // we chose to stop the scan.
            scanning = false;
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    /**
     * This callback handles behavior associated with BLE scanning. The one function inside of it
     * deals with what happens when you find a device.
     */
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            /* This makes sure that the device is in the list, and that it has a name that
            contains HET. If the HET device no longer has a value in the name field, you MUST
            change this. */
            if (notDuplicate(device) && device.getName() != null) {
                //add the device to the list and update the UI.
                deviceListAdapter.add(new BluetoothDeviceInfoWrapper(device));
                deviceListAdapter.notifyDataSetChanged();
            }

        }

        /**
         * makes sure the device doesn't exist in the list already
         *
         * @param device the device you found
         * @return whether the device is in the list or not
         */
        private boolean notDuplicate(BluetoothDevice device) {
            for (int i = 0; i < deviceListAdapter.getCount(); i++) {
                if (deviceListAdapter.getItem(i).toString().equals((new BluetoothDeviceInfoWrapper(device)).toString())) {
                    return false;
                }
            }
            return true;
        }
    };


    /**
     * What happens when you click a button on the screen.
     * For btScan, the GATT server scan is started.
     * for btRefresh, the list is refreshed and the scan is restarted
     * for btDone, it figures out how many devices are checked and sends the device information
     * to the next screen or tells you to hold up.
     *
     * @param v the button clicked
     */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btScanButton:
                //start the scan, hide this button and show the others
                scanLeDevice(true);
                findViewById(R.id.btScanButton).setVisibility(View.INVISIBLE);
                findViewById(R.id.btRefresh).setVisibility(View.VISIBLE);
                findViewById(R.id.btDone).setVisibility(View.VISIBLE);
                break;
            case R.id.btRefresh:
                //refresh and start scanning again
                if (scanning) {
                    scanLeDevice(false);
                }
                deviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.btDone:
                //make sure you haven't checked too many or too little and yells at you if so
                if (devicesListView.getCheckedItemCount() == 0) {
                    Toast.makeText(this, R.string.pickOneDummy, Toast.LENGTH_SHORT).show();
                } else if (devicesListView.getCheckedItemCount() > 2) {
                    Toast.makeText(this, R.string.tooManyDevices, Toast.LENGTH_SHORT).show();
                } else {
                    //find the first device in the list
                    BluetoothDevice device1 = findFirstDevice();

                    //create an intent that sends data to the next activity
                    final Intent intent = new Intent(this, SettingsActivity.class);
                    //put the device info in the extras field
                    intent.putExtra(DashboardActivity.EXTRAS_DEVICE_NAME_ONE, device1.getName());
                    intent.putExtra(DashboardActivity.EXTRAS_DEVICE_ADDRESS_ONE, device1.getAddress());

                    //look for a second device, put that data in the extras field if there is one
                    BluetoothDevice device2 = findSecondDevice();
                    if (devicesListView.getCheckedItemCount() != 2 || device2 == null) {
                        intent.putExtra(DashboardActivity.EXTRAS_DEVICE_NAME_TWO, "null");
                        intent.putExtra(DashboardActivity.EXTRAS_DEVICE_ADDRESS_TWO, "null");
                    } else {
                        intent.putExtra(DashboardActivity.EXTRAS_DEVICE_NAME_TWO,
                                device2.getName());
                        intent.putExtra(DashboardActivity.EXTRAS_DEVICE_ADDRESS_TWO,
                                device2.getAddress());
                    }
                    //go on to the settings activity
                    startActivity(intent);
                }
                break;
        }
    }

    /**
     * Looks through the list of devices and finds the first one checked
     *
     * @return the first checked device in the list
     */
    private BluetoothDevice findFirstDevice() {
        for (int i = 0; i < deviceListAdapter.getCount(); i++) {
            System.out.println("device name: " + deviceListAdapter.getItem(i).toString() + " is " +
                    "checked: " + devicesListView.isItemChecked(i));
            if (devicesListView.isItemChecked(i)) {
                BluetoothDeviceInfoWrapper wrapper = deviceListAdapter.getItem(i);
                return wrapper.getDevice();
            }
        }
        return null;
    }

    /**
     * looks through the list of devices and finds the second one checked
     *
     * @return the second checked device, null if there isn't one
     */
    private BluetoothDevice findSecondDevice() {
        boolean skipOne = false;
        for (int i = 0; i < deviceListAdapter.getCount(); i++) {
            System.out.println(
                    "device name: " + deviceListAdapter.getItem(i).toString() + " is checked: " +
                            devicesListView.isItemChecked(i));
            if (devicesListView.isItemChecked(i)) {
                if (!skipOne) {
                    skipOne = true;
                } else {
                    return deviceListAdapter.getItem(i).getDevice();
                }
            }
        }
        return null;
    }


    private static final long SCAN_PERIOD = 10000;
    private static int REQUEST_ENABLE_GPS = 6275;
    private static int REQUEST_ENABLE_BT = 6274;
}
