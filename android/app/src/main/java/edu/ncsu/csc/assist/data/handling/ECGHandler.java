package edu.ncsu.csc.assist.data.handling;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import edu.ncsu.csc.assist.data.objects.ECGData;

public class ECGHandler {

    private static final int BYTES_PER_VALUE = 3;
    private static final int NUMBER_OF_VALUES = 4;

    private static List<ECGData> ecgHistory = new LinkedList<ECGData>();

    /**
     * Expects 12 bytes of ecg data sent from the HET Chest device
     * Each 3 bytes are one reading and are generated 5ms apart
     * This method should run approximately every 20 ms
     *
     * @param buffer    12 bytes of ecg data
     * @param timestamp the time that the first data value in the buffer was read
     */
    public static void handle(byte[] buffer, long timestamp) {
        for (int i = 0; i < NUMBER_OF_VALUES * BYTES_PER_VALUE; i += BYTES_PER_VALUE) {
            byte[] dataBytes = {0, buffer[i], buffer[i + 1], buffer[i + 2]};
            ByteBuffer wrapped = ByteBuffer.wrap(dataBytes);
            int reading = wrapped.getInt();
            ecgHistory.add(new ECGData(reading, timestamp + i * 5));
            sendRawData(ecgHistory.get(ecgHistory.size() - 1));
        }
        double heartRate = determineHeartRate();
        sendProcessedData(heartRate, timestamp);
    }

    /**
     * performs analysis on the ecg data to determine heart rate
     *
     * @return heart rate
     */
    private static double determineHeartRate() {
        return 0;
    }

    /**
     * Sends raw data to the raw data database buffer
     *
     * @param dataPoint an ECGData object that holds the reading and the time it was recorded
     */
    private static void sendRawData(ECGData dataPoint) {

    }

    /**
     * Sends processed data to the processed database buffer
     * -processed data in this case means estimated heart rate
     *
     * @param heartrate calculated heart rate from the ecg data
     * @param timestamp the time that the heart rate was recorded
     */
    private static void sendProcessedData(double heartrate, long timestamp) {

    }

}
