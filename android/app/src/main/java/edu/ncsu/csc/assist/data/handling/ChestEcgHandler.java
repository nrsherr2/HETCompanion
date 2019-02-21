package edu.ncsu.csc.assist.data.handling;

import java.util.LinkedList;
import java.util.List;

import edu.ncsu.csc.assist.data.cloud.DataStorer;
import edu.ncsu.csc.assist.data.objects.EcgData;

public class ChestEcgHandler extends Handler {

    private static final int BYTES_PER_VALUE = 3;
    private static final int NUMBER_OF_VALUES = 4;
    private static final int MILLIS_BETWEEN_VALUES = 5;

    private List<EcgData> ecgHistory = new LinkedList<EcgData>();

    public ChestEcgHandler(DataStorer rawDataBuffer) {
        super(BYTES_PER_VALUE, NUMBER_OF_VALUES, rawDataBuffer);
    }

    /**
     * Expects 12 bytes of ecg data sent from the HET Chest device
     * Each 3 bytes are one reading and are generated 5ms apart
     * This method should run approximately every 20 ms
     *
     * @param buffer    12 bytes of ecg data
     * @param timestamp the time that the first data value in the buffer was read
     */
    @Override
    public void handle(byte[] buffer, long timestamp) {
        for (int i = 0; i < getTotalByteSize(); i += getBytesPerValue()) {
            int reading = getIntFromBytes(buffer[i], buffer[i + 1], buffer[i + 2]);
            EcgData dataPoint = new EcgData(reading, timestamp + i * MILLIS_BETWEEN_VALUES);
            ecgHistory.add(dataPoint);
            sendRawData(dataPoint);
        }
        double heartRate = determineHeartRate();
        sendProcessedData(heartRate, timestamp);
    }

    /**
     * performs analysis on the ecg data to determine heart rate
     *
     * @return heart rate
     */
    private double determineHeartRate() {
        return 0;
    }

    /**
     * Sends processed data to the processed database buffer
     * -processed data in this case means estimated heart rate
     *
     * @param heartrate calculated heart rate from the ecg data
     * @param timestamp the time that the heart rate was recorded
     */
    private void sendProcessedData(double heartrate, long timestamp) {

    }

}
