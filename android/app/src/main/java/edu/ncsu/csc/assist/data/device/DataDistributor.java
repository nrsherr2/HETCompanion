package edu.ncsu.csc.assist.data.device;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import edu.ncsu.csc.assist.data.handling.ECGHandler;

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

    private static int CHEST_ECG_BYTES = 12;
    private static int CHEST_PPG_BYTES = 8;
    private static int CHEST_INERTIAL_BYTES = 12;
    private static int CHEST_DATA_BYTES = CHEST_ECG_BYTES + CHEST_PPG_BYTES + CHEST_INERTIAL_BYTES;

    private static int WRIST_INERTIAL_BYTES = 12;
    private static int WRIST_PPG_BYTES = 4;
    private static int WRIST_OZ_BYTES = 2;
    private static int WRIST_POZ_BYTES = 2;
    private static int WRIST_ROZ_BYTES = 2;
    private static int WRIST_MOZ_BYTES = 2;
    private static int WRIST_TMP_BYTES = 2;
    private static int WRIST_HUMID_BYTES = 2;
    private static int WRIST_DATA_BYTES = WRIST_INERTIAL_BYTES + WRIST_PPG_BYTES + WRIST_OZ_BYTES + WRIST_POZ_BYTES + WRIST_ROZ_BYTES + WRIST_MOZ_BYTES + WRIST_TMP_BYTES + WRIST_HUMID_BYTES;

    public static void distributeChestData(byte[] data, long timestamp) {

        if (data.length != CHEST_DATA_BYTES) {
            throw new IllegalArgumentException("HET Chest data received did not match expected length");
        }
        List<Byte> listedData = new ArrayList(Arrays.asList(data));
        Iterator<Byte> iterator = listedData.iterator();

        //--CHEST DATA PARSING--
        //ECG data
        byte[] ecgData = new byte[CHEST_ECG_BYTES];
        for (int i = 0; i < CHEST_ECG_BYTES; i++) {
            ecgData[i] = iterator.next();
        }
        //PPG data
        byte[] ppgData = new byte[CHEST_PPG_BYTES];
        for (int i = 0; i < CHEST_PPG_BYTES; i++) {
            ppgData[i] = iterator.next();
        }
        //Inertial data
        byte[] inertialData = new byte[CHEST_INERTIAL_BYTES];
        for (int i = 0; i < CHEST_INERTIAL_BYTES; i++) {
            inertialData[i] = iterator.next();
        }

        ECGHandler.handle(ecgData, timestamp);
        //ChestPPGHandler.handle(ppgData, timestamp);
        //ChestInertialHandler.handle(inertialHandler, timestamp);
    }

    public static void distributeWristData(byte[] data, long timestamp) {

        if (data.length != WRIST_DATA_BYTES) {
            throw new IllegalArgumentException("HET Wrist data received did not match expected length");
        }
        List<Byte> listedData = new ArrayList(Arrays.asList(data));
        Iterator<Byte> iterator = listedData.iterator();

        //--WRIST DATA PARSING--
        //Inertial data
        byte[] inertialData = new byte[WRIST_INERTIAL_BYTES];
        for (int i = 0; i < WRIST_INERTIAL_BYTES; i++) {
            inertialData[i] = iterator.next();
        }
        //PPG data
        byte[] ppgData = new byte[WRIST_PPG_BYTES];
        for (int i = 0; i < WRIST_PPG_BYTES; i++) {
            ppgData[i] = iterator.next();
        }
        //oz data
        byte[] ozData = new byte[WRIST_OZ_BYTES];
        for (int i = 0; i < WRIST_OZ_BYTES; i++) {
            ozData[i] = iterator.next();
        }
        //poz data
        byte[] pozData = new byte[WRIST_POZ_BYTES];
        for (int i = 0; i < WRIST_POZ_BYTES; i++) {
            pozData[i] = iterator.next();
        }
        //roz data
        byte[] rozData = new byte[WRIST_ROZ_BYTES];
        for (int i = 0; i < WRIST_ROZ_BYTES; i++) {
            rozData[i] = iterator.next();
        }
        //moz data
        byte[] mozData = new byte[WRIST_MOZ_BYTES];
        for (int i = 0; i < WRIST_MOZ_BYTES; i++) {
            mozData[i] = iterator.next();
        }
        //tmp data
        byte[] tmpData = new byte[WRIST_TMP_BYTES];
        for (int i = 0; i < WRIST_TMP_BYTES; i++) {
            tmpData[i] = iterator.next();
        }
        //humid data
        byte[] humidData = new byte[WRIST_HUMID_BYTES];
        for (int i = 0; i < WRIST_HUMID_BYTES; i++) {
            humidData[i] = iterator.next();
        }


        //send all the data off to respective handlers...
    }
}
