package edu.ncsu.csc.assist.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import androidx.annotation.Nullable;

/**
 * Service for managing connection and data communication with a GATT server on a BLE device.
 * <p>
 * This was adapted from Google's sample code for a BLE project.
 *
 * @see <a href="https://developer.android.com/guide/topics/connectivity/bluetooth-le">Google BLE tutorial</a>
 * @see <a href="https://github.com/googlesamples/android-BluetoothLeGatt/blob/master/Application/src/main/java/com/example/android/bluetoothlegatt/BluetoothLeService.java">BluetoothLeService.java Google sample project</a>
 */
public class BluetoothLeService extends Service {

    private final UUID fff0 = new UUID(0xfff000001000L, 0x800000805f9b34fbL);
    private final UUID fff3 = new UUID(0xfff300001000L, 0x800000805f9b34fbL);
    private final UUID fff1 = new UUID(0x0000fff100001000L, 0x800000805f9b34fbL);
    public static final String EXTRA_DATA = "edu.ncsu.csc.assist.EXTRA_DATA";
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    public static final String DATA_AVAILABLE = "edu.ncsu.csc.assist.ACTION_DATA_AVAILABLE";
    public static final String ACTION_GATT_CONNECTED = "edu.ncsu.csc.assist.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = "edu.ncsu.csc.assist" +
            ".ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED = "edu.ncsu.csc.assist" +
            ".ACTION_GATT_SERVICES_DISCOVERED";
    private BluetoothGatt mBluetoothGatt;
    private String deviceAddress;
    private static final UUID CLIENT_CHARACTERISTIC_CONFIGURATION = new UUID(0x290200001000L, 0x800000805f9b34fbL);

    /**
     * Initializes a reference to the Bluetooth Adapter
     *
     * @return true if initialization is successful
     */
    public boolean initialize() {
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                System.out.println("could not initialize bluetooth manager");
                return false;
            }
        }
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            System.out.println("unable to obtain bluetooth adapter");
            return false;
        }
        return true;
    }


    /**
     * Connects to the GATT server on the BLE device
     *
     * @param address the address of the destination device
     * @return true if you intiated the connection. The connection result is reported in
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     */
    public boolean connect(final String address) {
        if (bluetoothAdapter == null || address == null) {
            System.out.println("necessary information not initialized");
            return false;
        }
        //in the case that this was a previously connected device
        if (address.equals(deviceAddress) && mBluetoothGatt != null) {
            System.out.println("trying to use an existing bluetooth connection for connection");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            System.out.println("Device not found. Unable to connect.");
            return false;
        }
        //now connect
        mBluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);
        System.out.println("Creating a new connection");
        deviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancels a pending connection. The result is reported in
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     */
    public void disconnect() {
        if (bluetoothAdapter == null || mBluetoothGatt == null) {
            System.out.println("adapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * close the BluetoothGatt to ensure garbage is collected properly
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * integers that define connection states
     */
    private static final int STATE_DISCONNECTED = 0, STATE_CONNECTING = 1, STATE_CONNECTED = 2;
    /**
     * Integer that checks the device connection state
     */
    private int mConnectionState = STATE_DISCONNECTED;

    /**
     * Connecting to GATT devices requires a callback whenever a bluetooth action is taken, so this class deals with any behaviors that may arise.
     */
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        /**
         * Instructions for what to do when connection state changes
         * @param gatt the Bluetooth GATT profile for the connection
         * @param status the status the connect or disconnect operation
         * @param newState the new connection state
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intent;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intent = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intent);
                System.out.println("Connected to GATT server");
                //starts service discovery
                System.out.println("Starting service discovery: " + mBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intent = ACTION_GATT_DISCONNECTED;
                System.out.println("Disconnected from GATT server");
                broadcastUpdate(intent);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                System.out.println("onServicesDiscovered received " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gett, BluetoothGattCharacteristic characteristic, int status) {
            System.out.println("read status: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            System.out.println("received update with changed info " + Arrays.toString(bluetoothGattCharacteristic.getValue()));
            readCharacteristic(bluetoothGattCharacteristic);
            broadcastUpdate(DATA_AVAILABLE, bluetoothGattCharacteristic);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            System.out.println("descriptor was written, status " + status);
            if (startStream(fff0, fff1)) {
                System.out.println("started stream");
            } else {
                System.out.println("could not start stream");
            }
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        //there's some stuff in the sample project for a heart rate management special case, but it doesn't apply to this project
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data)
                stringBuilder.append(String.format("%02x ", byteChar));
            intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
        }
        sendBroadcast(intent);
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. Read result reported in
     * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     *
     * @param characteristic the characteristic to read from
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bluetoothAdapter == null || mBluetoothGatt == null) {
            System.out.println("adapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public boolean setCharacteristicNotification(BluetoothGattCharacteristic c, boolean enabled) {
        if (bluetoothAdapter == null || mBluetoothGatt == null) {
            System.out.println("adapter not initialized");
            return false;
        }
        BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService(c.getService().getUuid()).getCharacteristic(c.getUuid());
        boolean changed = mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIGURATION);
        if (descriptor == null) {
            System.out.println("could not find descriptor " + CLIENT_CHARACTERISTIC_CONFIGURATION.toString());
        } else {
            if (descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
                if (mBluetoothGatt.writeDescriptor(descriptor)) {
                    System.out.println("Changed descriptor to enable notification value");
                } else {
                    System.out.println("could not set up write command");
                    System.out.println(descriptor.getPermissions());
                    System.out.println(descriptor.getCharacteristic().getUuid().toString());
                    System.out.println(descriptor.getCharacteristic().getWriteType());
                }
            } else {
                System.out.println("did not change descriptor locally");
            }
        }
        return changed;
        //theres some other specific stuff in the example, we don't need it
    }


    public boolean startStream(UUID serviceID, UUID characteristicID) {
        BluetoothGattService service = mBluetoothGatt.getService(serviceID);
        if (service == null) {
            System.out.print("cannot find required service.");
            return false;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicID);
        if (characteristic == null || (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) == 0) {
            System.out.println("characteristic we want to write to not found.");
            return false;
        } else {
            characteristic.setValue(new byte[]{0x02});
            return mBluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    public BluetoothGattCharacteristic findAndSetNotify(UUID serviceID, UUID characteristicID) {
        BluetoothGattService service = mBluetoothGatt.getService(serviceID);
        if (service == null) {
            System.out.println("cannot find required service.");
            return null;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicID);
        if (characteristic == null || (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0) {
            System.out.println("characteristic we're looking for with that notify property doesn't exist.");
            return null;
        } else {
            if (setCharacteristicNotification(characteristic, true)) {
                return characteristic;
            } else {
                return null;
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close(); // clear up the connections
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getServices();
    }
}
