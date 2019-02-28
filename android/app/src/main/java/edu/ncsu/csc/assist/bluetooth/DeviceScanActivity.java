package edu.ncsu.csc.assist.bluetooth;

import android.Manifest;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import edu.ncsu.csc.assist.R;

public class DeviceScanActivity extends ListActivity {


    private LeDeviceListAdapter leDeviceListAdapter;
    private Handler handler;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        System.out.println("Device scan created");
        super.onCreate(savedInstanceState);
        handler = new Handler();

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.bl_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //setContentView(R.layout.bluetooth_connect);
    }

    // used for enabling bt
    private static int REQUEST_ENABLE_GPS = 6275;
    private static int REQUEST_ENABLE_BT = 6274;

    @Override
    protected void onResume() {
        super.onResume();
        //make sure location is still allowed
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ENABLE_GPS);
        }
        //make sure bluetooth is still allowed
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        leDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(leDeviceListAdapter);
        scanLeDevice(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        leDeviceListAdapter.clear();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final BluetoothDevice device = leDeviceListAdapter.getDevice(position);
        if (device == null) return;
        final Intent intent = new Intent(this, DeviceControlActivity.class);
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        if (scanning) {
            bluetoothAdapter.stopLeScan(leScanCallback);
            scanning = false;
        }
        startActivity(intent);
    }

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private boolean scanning;

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


    /**
     * adapter for holding devices found through scanning
     */
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> leDevices;
        private LayoutInflater inflator;

        public LeDeviceListAdapter() {
            super();
            leDevices = new ArrayList<>();
            inflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if (!leDevices.contains(device)) {
                leDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return leDevices.get(position);
        }

        public void clear() {
            leDevices.clear();
        }

        @Override
        public int getCount() {
            return leDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return leDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                System.out.println("null");
                view = inflator.inflate(R.layout.device_list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = view.findViewById(R.id.device_address);
                viewHolder.deviceName = view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            BluetoothDevice device = leDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0) {
                viewHolder.deviceName.setText(deviceName);
            } else {
                viewHolder.deviceName.setText(R.string.unknowndevice);
            }
            viewHolder.deviceAddress.setText(device.getAddress());
            return view;
        }

    }

//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.btScanButton:
//                v.findViewById(R.id.btStopScanButton).setVisibility(View.VISIBLE);
//                v.findViewById(R.id.btScanButton).setVisibility(View.INVISIBLE);
//                leDeviceListAdapter.clear();
//                scanLeDevice(true);
//                break;
//            case R.id.btStopScanButton:
//                v.findViewById(R.id.btScanButton).setVisibility(View.VISIBLE);
//                v.findViewById(R.id.btStopScanButton).setVisibility(View.INVISIBLE);
//                scanLeDevice(false);
//        }
//    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    leDeviceListAdapter.addDevice(device);
                    leDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };


    static class ViewHolder {
        TextView deviceName, deviceAddress;
    }
}
