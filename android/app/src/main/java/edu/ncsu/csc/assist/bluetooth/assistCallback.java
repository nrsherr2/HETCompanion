package edu.ncsu.csc.assist.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class assistCallback implements BluetoothAdapter.LeScanCallback {

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

    }
}
