package edu.ncsu.csc.assist.data.device;

import java.util.Arrays;

import edu.ncsu.csc.assist.data.handling.ECGHandler;
import edu.ncsu.csc.assist.data.handling.Handler;

/**
 * Data Package Structure
 * <p>
 * HET Chest: 32 Bytes
 * |ecg1|ecg1|ecg1|ecg2|ecg2|ecg2|ecg3|ecg3|ecg3|ecg4|ecg4|ecg4|
 * |ppg1|ppg1|ppg2|ppg2|ppg3|ppg3|ppg4|ppg4|
 * | x1 | x1 | y1 | y1 | z1 | z1 | x2 | x2 | y2 | y2 | z2 | z2 |
 * <p>
 * HET Wrist: 28 Bytes
 * | x1 | x1 | y1 | y1 | z1 | z1 | x2 | x2 | y2 | y2 | z2 | z2 |
 * |ppg1|ppg1|ppg2|ppg2|
 * |oz1 |oz1 |poz1|poz1|roz1|roz1|moz1|moz1|tmp1|tmp1|humid1|humid1|
 */
public class DataDistributor {

    private static Handler ecgHandler;
    //many other handlers will go here

    //once other handlers are created, the below values will only be initialized through the constructor
    private int CHEST_ECG_BYTES = 12;
    private int CHEST_PPG_BYTES = 8;
    private int CHEST_INERTIAL_BYTES = 12;
    private int CHEST_DATA_BYTES = CHEST_ECG_BYTES + CHEST_PPG_BYTES + CHEST_INERTIAL_BYTES;

    private int WRIST_INERTIAL_BYTES = 12;
    private int WRIST_PPG_BYTES = 4;
    private int WRIST_OZ_BYTES = 2;
    private int WRIST_POZ_BYTES = 2;
    private int WRIST_ROZ_BYTES = 2;
    private int WRIST_MOZ_BYTES = 2;
    private int WRIST_TMP_BYTES = 2;
    private int WRIST_HUMID_BYTES = 2;
    private int WRIST_DATA_BYTES = WRIST_INERTIAL_BYTES + WRIST_PPG_BYTES + WRIST_OZ_BYTES + WRIST_POZ_BYTES + WRIST_ROZ_BYTES + WRIST_MOZ_BYTES + WRIST_TMP_BYTES + WRIST_HUMID_BYTES;

    public DataDistributor() {
        ecgHandler = new ECGHandler();
        //many other handlers will be initialized here

        CHEST_ECG_BYTES = ecgHandler.getTotalByteSize();
        //many other values will be initialized here.
    }

    public void distributeChestData(byte[] data, long timestamp) {

        if (data.length != CHEST_DATA_BYTES) {
            throw new IllegalArgumentException("HET Chest data received did not match expected length");
        }
        int offset = 0;

        //--CHEST DATA PARSING--
        //ECG data
        byte[] ecgData = Arrays.copyOfRange(data, offset, offset + CHEST_ECG_BYTES);
        offset += CHEST_ECG_BYTES;

        //PPG data
        byte[] ppgData = Arrays.copyOfRange(data, offset, offset + CHEST_PPG_BYTES);
        offset += CHEST_PPG_BYTES;

        //Inertial data
        byte[] inertialData = Arrays.copyOfRange(data, offset, offset + CHEST_INERTIAL_BYTES);
        offset += CHEST_INERTIAL_BYTES;

        ecgHandler.handle(ecgData, timestamp);
        //ChestPPGHandler.handle(ppgData, timestamp);
        //ChestInertialHandler.handle(inertialHandler, timestamp);
    }

    public void distributeWristData(byte[] data, long timestamp) {

        if (data.length != WRIST_DATA_BYTES) {
            throw new IllegalArgumentException("HET Wrist data received did not match expected length");
        }
        int offset = 0;

        //--WRIST DATA PARSING--
        //Inertial data
        byte[] inertialData = Arrays.copyOfRange(data, offset, offset + WRIST_INERTIAL_BYTES);
        offset += WRIST_INERTIAL_BYTES;

        //PPG data
        byte[] ppgData = Arrays.copyOfRange(data, offset, offset + WRIST_PPG_BYTES);
        offset += WRIST_PPG_BYTES;

        //oz data
        byte[] ozData = Arrays.copyOfRange(data, offset, offset + WRIST_OZ_BYTES);
        offset += WRIST_OZ_BYTES;

        //poz data
        byte[] pozData = Arrays.copyOfRange(data, offset, offset + WRIST_POZ_BYTES);
        offset += WRIST_POZ_BYTES;

        //roz data
        byte[] rozData = Arrays.copyOfRange(data, offset, offset + WRIST_ROZ_BYTES);
        offset += WRIST_ROZ_BYTES;

        //moz data
        byte[] mozData = Arrays.copyOfRange(data, offset, offset + WRIST_MOZ_BYTES);
        offset += WRIST_MOZ_BYTES;

        //tmp data
        byte[] tmpData = Arrays.copyOfRange(data, offset, offset + WRIST_TMP_BYTES);
        offset += WRIST_TMP_BYTES;

        //humid data
        byte[] humidData = Arrays.copyOfRange(data, offset, offset + WRIST_HUMID_BYTES);
        offset += WRIST_HUMID_BYTES;


        //send all the data off to respective handlers...
    }
}
