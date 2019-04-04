package edu.ncsu.csc.assist.data.handling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.ncsu.csc.assist.data.cloud.DataStorer;
import edu.ncsu.csc.assist.data.cloud.ProcessedDataStorer;
import edu.ncsu.csc.assist.data.objects.GenericData;
import edu.ncsu.csc.assist.data.objects.ProcessedData;

public abstract class Handler {

    protected int bytesPerValue;

    protected int numberOfValues;

    protected int timeBetweenValues;

    private static final int intBitMask = 0x000000FF;

    private DataStorer rawDataBuffer;
    private ProcessedDataStorer processedDataBuffer;

    public Handler(int bytesPerValue, int numberOfValues, int timeBetweenValues, DataStorer rawDataBuffer, ProcessedDataStorer processedDataBuffer) {
        this.bytesPerValue = bytesPerValue;
        this.numberOfValues = numberOfValues;
        this.timeBetweenValues = timeBetweenValues;
        this.rawDataBuffer = rawDataBuffer;
        this.processedDataBuffer = processedDataBuffer;

    }


    public abstract void handle(byte[] buffer, long timestamp);

    public int getTotalByteSize() {
        return bytesPerValue * numberOfValues;
    }

    public int getBytesPerValue() {
        return bytesPerValue;
    }

    public int getNumberOfValues() {
        return numberOfValues;
    }

    static int getIntFromBytes(byte... bytes) {
        if(bytes.length == 0){
            return 0;
        }
        int newInt = bytes[0];
        for (int i = 1; i < bytes.length; i++) {
            newInt = newInt << 8;
            newInt = newInt | (bytes[i] & intBitMask);
        }
        return newInt;
    }

    /**
     * Sends raw data to the raw data database buffer
     *
     * @param dataPoint an GenericData object that holds the reading and the time it was recorded
     */
    protected void sendRawData(GenericData dataPoint) {
        rawDataBuffer.save(dataPoint);
    }

    /**
     * Sends raw data to the raw data database buffer
     *
     * @param dataPoints a list of GenericData objects that holds the reading and the time it was recorded
     */
    protected void sendRawData(List<GenericData> dataPoints) {
        rawDataBuffer.save(dataPoints);
    }

    /**
     * Sends processed data to the processed data database buffer
     *
     * @param dataPoint an GenericData object that holds the average reading and the average time it was recorded
     */
    protected void sendProcessedData(ProcessedData dataPoint) {
        processedDataBuffer.save(dataPoint);
    }

    /**
     * Sends processed data to the processed data database buffer
     *
     * @param dataPoints a list of GenericData objects that holds the average reading and the average time it was recorded
     */
    protected void sendProcessedData(List<ProcessedData> dataPoints) {
        processedDataBuffer.save(dataPoints);
    }

    protected List<GenericData> parseInput(byte[] input, long timestamp){
        List<GenericData> dataValues = new ArrayList<>();
        for (int i = 0; i < numberOfValues; i += 1) {
            dataValues.addAll(parseReading(Arrays.copyOfRange(input, i*bytesPerValue, (i+1)*bytesPerValue ), timestamp + i * timeBetweenValues));
        }
        return dataValues;
    }
    protected abstract List<GenericData> parseReading(byte[] data, long timestamp);
}
