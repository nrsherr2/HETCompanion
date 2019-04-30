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

public class BtButtonActivity extends Activity {
    private Handler handler;
    private BluetoothAdapter bluetoothAdapter;
    private ListView devicesListView;
    private ArrayAdapter<BluetoothDeviceInfoWrapper> deviceListAdapter;
    private boolean scanning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_connect);
        handler = new Handler();

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.bl_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        devicesListView = (ListView) findViewById(R.id.gatt_devices_list);
        //devicesListView.setItemsCanFocus(false);
        ArrayList<BluetoothDeviceInfoWrapper> deviceList;
        deviceList = new ArrayList<>();
        deviceListAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice, deviceList);
        devicesListView.setAdapter(deviceListAdapter);
    }

    @Override
    protected void onResume() {

        super.onResume();
        //make sure location is still allowed
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ENABLE_GPS);
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    bluetoothAdapter.stopLeScan(leScanCallback);
                }
            }, SCAN_PERIOD);
            scanning = true;
            bluetoothAdapter.startLeScan(leScanCallback);
        } else {
            scanning = false;
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (notDuplicate(device) && device.getName() != null) {
                deviceListAdapter.add(new BluetoothDeviceInfoWrapper(device));
                deviceListAdapter.notifyDataSetChanged();
            }

        }

        private boolean notDuplicate(BluetoothDevice device) {
            for (int i = 0; i < deviceListAdapter.getCount(); i++) {
                if (deviceListAdapter.getItem(i).toString().equals((new BluetoothDeviceInfoWrapper(device)).toString())) {
                    return false;
                }
            }
            return true;
        }
    };


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btScanButton:
                scanLeDevice(true);
                findViewById(R.id.btScanButton).setVisibility(View.INVISIBLE);
                findViewById(R.id.btRefresh).setVisibility(View.VISIBLE);
                findViewById(R.id.btDone).setVisibility(View.VISIBLE);
                break;
            case R.id.btRefresh:
                if (scanning) {
                    scanLeDevice(false);
                }
                deviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.btDone:
                System.out.println("done gg");
                if (devicesListView.getCheckedItemCount() == 0) {
                    Toast.makeText(this, R.string.pickOneDummy, Toast.LENGTH_SHORT).show();
                } else if (devicesListView.getCheckedItemCount() > 2) {
                    Toast.makeText(this, R.string.tooManyDevices, Toast.LENGTH_SHORT).show();
                } else {
                    BluetoothDevice device1 = findDeviceAndRemoveIt();
                    System.out.println("found device 1: " + device1.getName() + " gg");
                    //BluetoothDevice device2 = findDeviceAndRemoveIt();

                    final Intent intent = new Intent(this, SettingsActivity.class);
                    intent.putExtra(DashboardActivity.EXTRAS_DEVICE_NAME_ONE, device1.getName());
                    intent.putExtra(DashboardActivity.EXTRAS_DEVICE_ADDRESS_ONE,
                            device1.getAddress());
                    startActivity(intent);
                }
                break;
        }
    }

    private BluetoothDevice findDeviceAndRemoveIt() {
        for (int i = 0; i < deviceListAdapter.getCount(); i++) {
            if (devicesListView.isItemChecked(i)) {
                BluetoothDeviceInfoWrapper wrapper = deviceListAdapter.getItem(i);
                deviceListAdapter.remove(wrapper);
                return wrapper.getDevice();
            }
        }
        return null;
    }


    private static final long SCAN_PERIOD = 10000;
    private static int REQUEST_ENABLE_GPS = 6275;
    private static int REQUEST_ENABLE_BT = 6274;
}
