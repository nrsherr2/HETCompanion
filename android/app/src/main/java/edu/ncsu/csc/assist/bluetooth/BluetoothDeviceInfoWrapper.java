package edu.ncsu.csc.assist.bluetooth;

import android.bluetooth.BluetoothDevice;

/**
 * This class is a sort of wrapper for a temporary reference to a BLE device. This class is
 * interacted with in the BtButtonActivity, as a way to display the devices on the screen. I
 * really just implemented it because I wanted to control the toString of a BluetoothDevice.
 */
class BluetoothDeviceInfoWrapper {
    /**
     * the device you're keeping track of
     */
    private BluetoothDevice device;

    /**
     * Initialize this with a BLE device reference
     */
    public BluetoothDeviceInfoWrapper(BluetoothDevice device) {
        this.device = device;
    }

    /**
     * When you want to display the device in a list, use this toString method.
     *
     * @return the device's name followed by it's address
     */
    @Override
    public String toString() {
        if (device.getName() == null) {
            return "BLE Device (" + device.getAddress() + ")";
        } else {
            return device.getName() + " (" + device.getAddress() + ")";
        }
    }

    /**
     * When the bluetooth device is requested from the UI, return a reference to the device
     *
     * @return the device as stored in memory
     */
    public BluetoothDevice getDevice() {
        return device;
    }
}
