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

import java.util.UUID;
import java.util.concurrent.Semaphore;

import androidx.annotation.Nullable;
import edu.ncsu.csc.assist.SignInClientHolder;
import edu.ncsu.csc.assist.data.device.DataReceiver;

/**
 * Service used for communication with the BLE device. This allows the dashboard to abstract all
 * of the BLE stuff so that you just need to make simple calls in the dashboard.
 */
public class BluetoothLeService extends Service {

    /* integers that define connection states */
    private static final int STATE_DISCONNECTED = 0, STATE_CONNECTING = 1, STATE_CONNECTED = 2;
    /* Integer that checks the device connection state */
    private int mConnectionState = STATE_DISCONNECTED;
    /* these two manage the connection between the hardware and software, but I don't know which
    does what. Being honest here. But they're super important, so don't delete them.*/
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    /* A virtual representation in memory of the BLE device. Call operations on it like it is
    actually the device. */
    private BluetoothGatt gattDeviceOne;
    private BluetoothGatt gattDeviceTwo;
    /* the MAC address of the device */
    private String deviceAddress1;
    private String deviceAddress2;
    /* This UUID corresponds to the descriptor of a characteristic we want a notification from */
    private static final UUID CLIENT_CHARACTERISTIC_CONFIGURATION = new UUID(0x290200001000L,
            0x800000805f9b34fbL);

    /**
     * The way BLE works with Android, there is one hard rule that nobody tells you at first, but
     * is super important: you may only have one pending write request to a GATT server at a time
     * . This is an issue to us, as we want to write a bunch of stuff to a device every time we
     * connect to it. We want to write a value to fff1 to start it, and change the descriptors of
     * each characteristic to make sure we start notifications. We use this semaphore here to
     * ensure that only one pending write is going out at a time. Whenever you start a write, you
     * acquire the lock, and whenever the callback is received saying the write went through, you
     * release the lock.
     */
    Semaphore writeLock = new Semaphore(1);

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

        //Initializes DataReceiver
        DataReceiver.initialize(this, SignInClientHolder.getClient());

        return true;
    }


    /**
     * Connects to the Android device to the GATT server on the HET device.
     *
     * @param address   the address of the destination device
     * @param deviceNum 1 or 2, depending on which device you want to connect to
     * @return true if you initiated the connection. The connection result is reported in
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int,
     * int)}
     */
    public boolean connect(final String address, int deviceNum) {
        //acquire the write lock
        try {
            writeLock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //make sure everything is initialize
        if (bluetoothAdapter == null || address == null) {
            System.out.println("necessary information not initialized");
            return false;
        }
        //in the case that this was a previously connected device
        if (address.equals(deviceAddress1) && gattDeviceOne != null) {
            if (gattDeviceOne.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }
        //in the case this was the previously connected second device
        if (address.equals(deviceAddress2) && gattDeviceTwo != null) {
            if (gattDeviceTwo.connect()) {
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
        if (deviceNum == 1) {
            gattDeviceOne = device.connectGatt(this, true, bluetoothGattCallback);
            deviceAddress1 = address;
            mConnectionState = STATE_CONNECTING;
        } else {
            gattDeviceTwo = device.connectGatt(this, true, bluetoothGattCallback2);
            deviceAddress2 = address;
            mConnectionState = STATE_CONNECTING;
        }
        return true;
    }

    /**
     * Disconnects an existing connection or cancels a pending connection. The result is reported in
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int,
     * int)}
     */
    public void disconnect() {
        if (bluetoothAdapter == null || gattDeviceOne == null) {
            System.out.println("adapter not initialized");
            return;
        }
        gattDeviceOne.disconnect();

        if (gattDeviceTwo != null) {
            gattDeviceTwo.disconnect();
        }
    }

    /**
     * close the BluetoothGatt to ensure garbage is collected properly
     */
    public void close() {
        if (gattDeviceOne == null) {
            return;
        }
        gattDeviceOne.close();
        gattDeviceOne = null;
        if (gattDeviceTwo != null) {
            gattDeviceTwo.close();
        }
    }


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
     * asynchronously sends out signals to any activities that may be listening containing messages
     *
     * @param action        the message you want to send out
     * @param deviceAddress the address of the device you are talking about
     */
    private void broadcastUpdate(final String action, final String deviceAddress) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_DATA, deviceAddress);
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
     * says whether we have a second device connected.
     *
     * @return whether the second device is connected or not
     */
    public boolean secondDeviceConnected() {
        return gattDeviceTwo != null;
    }

    /**
     * sends a call to asynchronously read information from the bluetooth device. Creates a
     * callback for reading a characteristic
     * We don't use this in the current version of the app, but hey, it's nice to have here.
     *
     * @param characteristic the characteristic that you want to read.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bluetoothAdapter == null || gattDeviceOne == null) {
            System.out.println("adapter not initialized");
            return;
        }
        gattDeviceOne.readCharacteristic(characteristic);
    }


    /**
     * This method is called on the way for setting up notifications for a characteristic. This
     * actually sets up the notifications so the callbacks are triggered when the info updates.
     *
     * @param serviceID        the service the characteristic is located in
     * @param characteristicID the characteristic we are looking for
     * @param deviceNum        1 or 2, depending on the device we are using.
     * @return whether we successfully set up notifications on the device.
     */
    public boolean setCharacteristicNotification(UUID serviceID, UUID characteristicID,
                                                 int deviceNum) {
        //check if everything is initialized
        if (bluetoothAdapter == null) {
            System.out.println("adapter not initialized");
            return false;
        }
        //get the write lock
        try {
            writeLock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //get characteristic
        BluetoothGattCharacteristic characteristic;
        boolean changed;
        if (deviceNum == 1) {
            //find the characteristic we're looking for
            characteristic =
                    gattDeviceOne.getService(serviceID).getCharacteristic(characteristicID);
            //tell the android device that we set the notification. this changes it locally
            changed = gattDeviceOne.setCharacteristicNotification(characteristic, true);
        } else {
            //find the characteristic we're looking for
            characteristic =
                    gattDeviceTwo.getService(serviceID).getCharacteristic(characteristicID);
            //tell the android device that we set the notification. this changes it locally
            changed = gattDeviceTwo.setCharacteristicNotification(characteristic, true);
        }
        /* we may have changed it on the Android device, but we still have to change the value on
         the HET device */
        //find the descriptor of our chosen characteristic
        BluetoothGattDescriptor descriptor =
                characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIGURATION);
        if (descriptor == null) {
            System.out.println(
                    "could not find descriptor " + CLIENT_CHARACTERISTIC_CONFIGURATION.toString() +
                            " for " + characteristicID);
            writeLock.release();
        } else {
            //set the local reference of the value
            if (descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {

                //write value to device
                if (deviceNum == 1 && gattDeviceOne.writeDescriptor(descriptor)) {
                    System.out.println("Changed the descriptor for characteristic " +
                            descriptor.getCharacteristic().getUuid().toString());
                } else if (deviceNum == 2 && gattDeviceTwo.writeDescriptor(descriptor)) {
                    System.out.println("Changed the descriptor for characteristic " +
                            descriptor.getCharacteristic().getUuid().toString());
                } else {
                    writeLock.release();
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
     * @param deviceNum        1 or 2, depending on which device we are talking to
     * @return whether you you successfully initiated a write to the device
     */
    public boolean startStream(UUID serviceID, UUID characteristicID, int deviceNum) {
        BluetoothGatt deviceOfInterest;
        if (deviceNum == 1) {
            deviceOfInterest = gattDeviceOne;
        } else {
            deviceOfInterest = gattDeviceTwo;
        }
        //find the service
        BluetoothGattService service = deviceOfInterest.getService(serviceID);
        if (service == null) {
            System.out.print("cannot find required service.");
            writeLock.release();
            return false;
        }
        //find the characteristic
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicID);
        //ensure you can write to it
        if (characteristic == null ||
                (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) ==
                        0) {
            System.out.println("characteristic we want to write to not found.");
            writeLock.release();
            return false;
        } else {
            //write the value now that we've ensured we can write to it
            characteristic.setValue(new byte[]{0x02});
            return deviceOfInterest.writeCharacteristic(characteristic);
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
     * @param deviceNum        1 or 2, depending on which device we care about
     * @return the characteristic that we are getting notifications on
     */
    public BluetoothGattCharacteristic findAndSetNotify(UUID serviceID, UUID characteristicID,
                                                        int deviceNum) {
        //find the service
        BluetoothGattService service;
        if (deviceNum == 1) {
            service = gattDeviceOne.getService(serviceID);
        } else {
            service = gattDeviceTwo.getService(serviceID);
        }
        if (service == null) {
            System.out.println("cannot find required service.");
            return null;
        }
        //find the characteristic
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicID);
        if (characteristic == null ||
                (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) ==
                        0) {
            System.out.println("characteristic we're looking for with that notify property " +
                    "doesn't exist.");
            return null;
        } else {
            //now that we know notifications are allowed, we call the method to do all that
            if (setCharacteristicNotification(serviceID, characteristicID, deviceNum)) {
                return characteristic;
            } else {
                return null;
            }
        }
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
            //if connected
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intent = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                //tell everyone that we connected successfully
                broadcastUpdate(intent);
                //starts service discovery
                gattDeviceOne.discoverServices();
                System.out.println("discovering services on " + gattDeviceOne.getDevice().getName());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //tell everyone we disconnected
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
            writeLock.release();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //tell everyone we found services on this device
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, gatt.getDevice().getName());
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
        public void onCharacteristicRead(BluetoothGatt gett, BluetoothGattCharacteristic characteristic, int status) {
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
        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            //tell everyone that the data updated, and what the data contains
            broadcastUpdate(DATA_AVAILABLE, bluetoothGattCharacteristic);
            //depending on which characteristic it came from, distribute the data to one of the
            // methods in the DataReceiver
            if (bluetoothGattCharacteristic.getUuid().toString().equals(fff3.toString())) {
                DataReceiver.receiveWristStreamTwo(bluetoothGattCharacteristic.getValue());
            } else if (bluetoothGattCharacteristic.getUuid().toString().equals(fff4.toString())) {
                DataReceiver.receiveWristStreamOne(bluetoothGattCharacteristic.getValue());
            } else if (bluetoothGattCharacteristic.getUuid().toString().equals(fff5.toString())) {
                DataReceiver.receiveChestStreamOne(bluetoothGattCharacteristic.getValue());
            } else if (bluetoothGattCharacteristic.getUuid().toString().equals(fff2.toString())) {
                DataReceiver.receiveChestStreamTwo(bluetoothGattCharacteristic.getValue());
            }
        }

        /**
         * Whenever the characteristic is written to, this callback is triggered.
         *
         * @param gatt           the device
         * @param characteristic the characteristic written to
         * @param status         whether the write was good or not
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            writeLock.release();
        }

        /**
         * when a descriptor is overwritten, this callback is initiated
         * @param gatt the device
         * @param descriptor the descriptor that was changed
         * @param status whether the write was a success
         */
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

            //this is callback 1, so we start the stream for device 1
            if (startStream(fff0, fff1, 1)) {
                System.out.println("started info stream");
            } else {
                System.out.println("could not start stream");
            }

        }
    };

    /**
     * One thing when connecting to 2 devices via BLE is that you need 2 separate callback
     * objects for each device. This is pretty much a copy of the other callback, but it deals
     * with the second device. If any future groups want to make it so updating one callback
     * updates both, please do. It's fine as it is for updating both separately, it's just a pain
     * making sure they're symmetrical in behavior. All of the comments in the last callback
     * apply to this one too.
     */
    private final BluetoothGattCallback bluetoothGattCallback2 = new BluetoothGattCallback() {
        /**
         * Instructions for what to do when connection state changes
         *
         * @param gatt     the Bluetooth GATT profile for the connection
         * @param status   the status the connect or disconnect operation
         * @param newState the new connection state
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intent;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //writeLock.release();
                intent = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intent);
                //starts service discovery
                gattDeviceTwo.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intent = ACTION_GATT_DISCONNECTED;
                broadcastUpdate(intent);
            }
        }

        /**
         * The system automatically searches for services on the device. This is that is called
         * when the BLE device tells the Android device what the services the BLE device has are.
         *
         * @param gatt   the GATT device
         * @param status whether the discovery was successful or not
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            writeLock.release();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, gatt.getDevice().getName());
            } else {
                System.out.println("onServicesDiscovered received " + status);
            }
        }

        /**
         * When a characteristic is read, this callback is received with the updated information
         *
         * @param gett           the device
         * @param characteristic the characteristic that is updated
         * @param status         if the read was successful or not
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gett, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(DATA_AVAILABLE, characteristic);
            }
        }

        /**
         * If you have notifications set up, this callback is triggered by a notification that
         * the characteristic changed
         *
         * @param bluetoothGatt               the bluetooth device
         * @param bluetoothGattCharacteristic the characteristic that was changed
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            broadcastUpdate(DATA_AVAILABLE, bluetoothGattCharacteristic);
            if (bluetoothGattCharacteristic.getUuid().toString().equals(fff3.toString())) {
                DataReceiver.receiveWristStreamTwo(bluetoothGattCharacteristic.getValue());
            } else if (bluetoothGattCharacteristic.getUuid().toString().equals(fff4.toString())) {
                DataReceiver.receiveWristStreamOne(bluetoothGattCharacteristic.getValue());
            } else if (bluetoothGattCharacteristic.getUuid().toString().equals(fff5.toString())) {
                DataReceiver.receiveChestStreamOne(bluetoothGattCharacteristic.getValue());
            } else if (bluetoothGattCharacteristic.getUuid().toString().equals(fff2.toString())) {
                DataReceiver.receiveChestStreamTwo(bluetoothGattCharacteristic.getValue());
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            writeLock.release();
        }

        /**
         * when a descriptor is overwritten, this callback is initiated
         *
         * @param gatt       the device
         * @param descriptor the descriptor that was changed
         * @param status     whether the write was a success
         */
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            //since we started the notifications, we also start the information stream


            if (startStream(fff0, fff1, 2)) {
                System.out.println("started info stream");
            } else {
                System.out.println("could not start stream");
            }

        }
    };

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


    /*
     * UUIDS that describe the characteristics and services we want to look for on the device.
     */
    private final UUID fff0 = new UUID(0xfff000001000L, 0x800000805f9b34fbL);
    private final UUID fff1 = new UUID(0x0000fff100001000L, 0x800000805f9b34fbL);
    private final UUID fff2 = new UUID(0xfff200001000L, 0x800000805f9b34fbL);
    private final UUID fff3 = new UUID(0xfff300001000L, 0x800000805f9b34fbL);
    private final UUID fff4 = new UUID(0xfff400001000L, 0x800000805f9b34fbL);
    private final UUID fff5 = new UUID(0xfff500001000L, 0x800000805f9b34fbL);
    /* Strings that indicate what kind of message is being sent. These are public because they're
     accessed elsewhere.*/
    public static final String EXTRA_DATA = "edu.ncsu.csc.assist.EXTRA_DATA";
    public static final String DATA_AVAILABLE = "edu.ncsu.csc.assist.ACTION_DATA_AVAILABLE";
    public static final String ACTION_GATT_CONNECTED = "edu.ncsu.csc.assist.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED =
            "edu.ncsu.csc.assist" + ".ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED =
            "edu.ncsu.csc.assist" + ".ACTION_GATT_SERVICES_DISCOVERED";

}
