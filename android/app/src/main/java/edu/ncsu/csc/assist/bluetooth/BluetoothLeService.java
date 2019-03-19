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
 * Service used for communication with the BLE device. This allows the dashboard to abstract all
 * of the BLE stuff so that you just need to make simple calls in the dashboard. You *may* have
 * to edit this when we scale to two devices at once.
 */
public class BluetoothLeService extends Service {

    /* integers that define connection states */
    private static final int STATE_DISCONNECTED = 0, STATE_CONNECTING = 1, STATE_CONNECTED = 2;
    /* Integer that checks the device connection state */
    private int mConnectionState = STATE_DISCONNECTED;
    /*
     * UUIDS that describe the characteristics and services we want to look for on the device.
     */
    private final UUID fff0 = new UUID(0xfff000001000L, 0x800000805f9b34fbL);
    private final UUID fff3 = new UUID(0xfff300001000L, 0x800000805f9b34fbL);
    private final UUID fff1 = new UUID(0x0000fff100001000L, 0x800000805f9b34fbL);
    /* these two manage the connection between the hardware and software, but I don't know which
    does what. Being honest here. But they're super important, so don't delete them.*/
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    /* Strings that indicate what kind of message is being sent. These are public because they're
     accessed elsewhere.*/
    public static final String EXTRA_DATA = "edu.ncsu.csc.assist.EXTRA_DATA";
    public static final String DATA_AVAILABLE = "edu.ncsu.csc.assist.ACTION_DATA_AVAILABLE";
    public static final String ACTION_GATT_CONNECTED = "edu.ncsu.csc.assist.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = "edu.ncsu.csc.assist" +
            ".ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED = "edu.ncsu.csc.assist" +
            ".ACTION_GATT_SERVICES_DISCOVERED";
    /* A virtual representation in memory of the BLE device. Call operations on it like it is
    actually the device. */
    private BluetoothGatt mBluetoothGatt;
    /* the MAC address of the device */
    private String deviceAddress;
    /* This UUID corresponds to the descriptor of a characteristic we want a notification from */
    private static final UUID CLIENT_CHARACTERISTIC_CONFIGURATION = new UUID(0x290200001000L,
            0x800000805f9b34fbL);

    /**
     * Initializes a reference to the Bluetooth Adapter
     *
     * @return true if initialization is successful
     */
    public boolean initialize() {
        //get the manager
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                System.out.println("could not initialize bluetooth manager");
                return false;
            }
        }
        //get the adapter
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            System.out.println("unable to obtain bluetooth adapter");
            return false;
        }
        return true;
    }


    /**
     * Connects to the Android device to the GATT server on the HET device.
     *
     * @param address the address of the destination device
     * @return true if you intiated the connection. The connection result is reported in
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int,
     * int)}
     */
    public boolean connect(final String address) {
        //make sure everything is initialize
        if (bluetoothAdapter == null || address == null) {
            System.out.println("necessary information not initialized");
            return false;
        }
        //in the case that this was a previously connected device
        if (address.equals(deviceAddress) && mBluetoothGatt != null) {
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }
        //call the adapter to find the device to connect to it
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
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int,
     * int)}
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
     * Whenever you communicate with a GATT device, the device issues a callback as a sort of
     * acknowledgement that it processed the data. This custom handler deals with all of the
     * callbacks this app is concerned with.
     * Basically all of these callbacks broadcast update data asynchronously back up to the
     * dashboard so that the dashboard can handle these updates.
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
                //starts service discovery
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intent = ACTION_GATT_DISCONNECTED;
                broadcastUpdate(intent);
            }
        }

        /**
         * The system automatically searches for services on the device. This is that is called
         * when the BLE device tells the Android device what the services the BLE device has are.
         * @param gatt the GATT device
         * @param status whether the discovery was successful or not
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                System.out.println("onServicesDiscovered received " + status);
            }
        }

        /**
         * When a characteristic is read, this callback is received with the updated information
         * @param gett the device
         * @param characteristic the characteristic that is updated
         * @param status if the read was successful or not
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gett,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(DATA_AVAILABLE, characteristic);
            }
        }

        /**
         * If you have notifications set up, this callback is triggered by a notification that
         * the characteristic changed
         * @param bluetoothGatt the bluetooth device
         * @param bluetoothGattCharacteristic the characteristic that was changed
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt,
                                            BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            //delete this line when you find something to do with this info.
            System.out.println("received update with changed info " + Arrays.toString(bluetoothGattCharacteristic.getValue()));
            //readCharacteristic(bluetoothGattCharacteristic);
            broadcastUpdate(DATA_AVAILABLE, bluetoothGattCharacteristic);
        }

        /**
         * when a descriptor is overwritten, this callback is initiated
         * @param gatt the device
         * @param descriptor the descriptor that was changed
         * @param status whether the write was a success
         */
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                      int status) {
            //since we started the notifications, we also start the information stream
            if (startStream(fff0, fff1)) {
                System.out.println("started info stream");
            } else {
                System.out.println("could not start stream");
            }
        }
    };

    /**
     * asynchronously sends out signals to any activities that may be listening containing messages
     *
     * @param action the message you want to send out
     */
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    /**
     * asynchronously sends out data about a bluetooth gatt characteristic to any activity that
     * may be listening.
     *
     * @param action         the action that should be taken as a result of this update
     * @param characteristic the characteristic that you want to send the value of
     */
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        //take the value of that characteristic and package it up so that it's transmitted as well
        final byte[] data = characteristic.getValue();
        //translate the byte array into an array of characters
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data)
                stringBuilder.append(String.format("%02x ", byteChar));
            intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
        }
        sendBroadcast(intent);
    }

    /**
     * sends a call to asynchronously read information from the bluetooth device. Creates a
     * callback for reading a characteristic
     *
     * @param characteristic the characteristic that you want to read.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bluetoothAdapter == null || mBluetoothGatt == null) {
            System.out.println("adapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }


    /**
     * This method is kinda sorta called within the
     *
     * @return whether we successfully set up notifications on the device.
     */
    public boolean setCharacteristicNotification(UUID serviceID, UUID characteristicID) {
        if (bluetoothAdapter == null || mBluetoothGatt == null) {
            System.out.println("adapter not initialized");
            return false;
        }
        //get characteristic fff3
        BluetoothGattCharacteristic characteristic =
                mBluetoothGatt.getService(serviceID).getCharacteristic(characteristicID);
        boolean enabled = true;
        //tell the android device that we set the notification
        boolean changed = mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        /* we may have changed it on the Android device, but we still have to change the value on
         the HET device */
        //find the descriptor of our chosen characteristic
        BluetoothGattDescriptor descriptor =
                characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIGURATION);
        if (descriptor == null) {
            System.out.println("could not find descriptor " + CLIENT_CHARACTERISTIC_CONFIGURATION.toString());
        } else {
            //set the local reference of the value
            if (descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
                //write that value to the device
                if (mBluetoothGatt.writeDescriptor(descriptor)) {
                    System.out.println("Changed descriptor to enable notification value");
                } else {
                    System.out.println("could not set up write command");
                }
            } else {
                System.out.println("did not change descriptor locally");
            }
        }
        return changed;
    }


    /**
     * The HET device requires an arbitrary value be written to the specific attribute on the
     * device. Give this method the service and the characteristic UUIDS and it will write an
     * arbitrary value to it.
     *
     * @param serviceID        the UUID of the service the characteristic is located in
     * @param characteristicID the UUID of the characteristic you're looking for
     * @return whether you you successfully initiated a write to the device
     */
    public boolean startStream(UUID serviceID, UUID characteristicID) {
        //find the service
        BluetoothGattService service = mBluetoothGatt.getService(serviceID);
        if (service == null) {
            System.out.print("cannot find required service.");
            return false;
        }
        //find the characteristic
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicID);
        //ensure you can write to it
        if (characteristic == null || (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) == 0) {
            System.out.println("characteristic we want to write to not found.");
            return false;
        } else {
            //write the value now that we've ensured we can write to it
            characteristic.setValue(new byte[]{0x02});
            return mBluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    /**
     * Call this method from the dashboard when you want to start streaming. it first checks that
     * the service and characteristic of fff0 and fff3 exist, then tries to set the notification
     * of the characteristic. When the callback is received, the service then tries to start the
     * stream.
     *
     * @param serviceID        the UUID of the service
     * @param characteristicID the UUID of the characteristic
     * @return the characteristic that we are getting notifications on
     */
    public BluetoothGattCharacteristic findAndSetNotify(UUID serviceID, UUID characteristicID) {
        //find the service
        BluetoothGattService service = mBluetoothGatt.getService(serviceID);
        if (service == null) {
            System.out.println("cannot find required service.");
            return null;
        }
        //find the characteristic
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicID);
        if (characteristic == null || (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0) {
            System.out.println("characteristic we're looking for with that notify property " +
                    "doesn't exist.");
            return null;
        } else {
            //now that we know notifications are allowed, we call the method to do all that
            if (setCharacteristicNotification(serviceID, characteristicID)) {
                return characteristic;
            } else {
                return null;
            }
        }
    }

    /**
     * binds the service to the activity so this service can be accessed by that activity and
     * that broadcasts can go to that activity.
     *
     * @param intent actions to take on bind
     * @return the specialty binder for the class
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * What to do when the service unbinds from the acitvity
     *
     * @param intent the actions to take
     * @return whether you safely unbound
     */
    @Override
    public boolean onUnbind(Intent intent) {
        close(); // clear up the connections
        return super.onUnbind(intent);
    }

    /**
     * binds this class to activities
     */
    private final IBinder mBinder = new LocalBinder();

    /**
     * class that contains bindings between this service and activities.
     */
    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

}
