package edu.ncsu.csc.assist.data.device;

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
    private static int CHEST_DATA_BYTES = 32;
    private static int CHEST_ECG_BYTES = 12;
    private static int CHEST_PPG_BYTES = 8;
    private static int CHEST_INERTIAL_BYTES = 12;

    private static int WRIST_DATA_BYTES = 28;

    /**
     * @return
     */
    public static byte[] recieveChestData() {
        byte[] chestData = new byte[CHEST_DATA_BYTES];
        return chestData;
    }

    /**
     * @return
     */
    public static byte[] recieveWristData() {
        byte[] wristData = new byte[WRIST_DATA_BYTES];
        return wristData;
    }

    public static void distributeData() {
        long timestamp = System.currentTimeMillis();
        byte[] chestData = recieveChestData();
        byte[] wristData = recieveWristData();

        if (chestData.length != CHEST_DATA_BYTES) {
            throw new IllegalArgumentException("Chest HET data received did not match expected length");
        }
        if (wristData.length != WRIST_DATA_BYTES) {
            throw new IllegalArgumentException("Wrist HET data received did not match expected length");
        }

        byte[] chestEcgData = new byte[CHEST_ECG_BYTES];
        for (int i = 0; i < CHEST_ECG_BYTES; i++) {
            chestEcgData[i] = chestData[i];
        }

        byte[] chestPpgData = new byte[CHEST_PPG_BYTES];
        for (int i = 0; i < CHEST_PPG_BYTES; i++) {
            chestPpgData[i] = chestData[CHEST_ECG_BYTES + i];
        }

        byte[] chestInertialData = new byte[CHEST_INERTIAL_BYTES];
        for (int i = 0; i < CHEST_INERTIAL_BYTES; i++) {
            chestInertialData[i] = chestData[CHEST_ECG_BYTES + CHEST_PPG_BYTES + i];
        }

        ECGHandler.handle(chestEcgData, timestamp);
        //ChestPPGHandler.handle(chestPpgData, timestamp);
        //ChestInertialHandler.handle(chestInertialData,timestamp);
    }
}
