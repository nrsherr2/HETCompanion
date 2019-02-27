package edu.ncsu.csc.assist.data.handling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.ncsu.csc.assist.data.cloud.DataStorer;
import edu.ncsu.csc.assist.data.objects.GenericData;

public abstract class Handler {

    protected int bytesPerValue;

    protected int numberOfValues;

    protected int timeBetweenValues;

    private DataStorer rawDataBuffer;

    public Handler(int bytesPerValue, int numberOfValues, int timeBetweenValues, DataStorer rawDataBuffer) {
        this.bytesPerValue = bytesPerValue;
        this.numberOfValues = numberOfValues;
        this.timeBetweenValues = timeBetweenValues;
        this.rawDataBuffer = rawDataBuffer;
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
        int newInt = 0;
        for (int i = 0; i < bytes.length; i++) {
            newInt = newInt << 8;
            newInt = newInt | bytes[i];
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

    protected List<GenericData> parseInput(byte[] input, long timestamp){
        List<GenericData> dataValues = new ArrayList<>();
        for (int i = 0; i < numberOfValues; i += 1) {
            dataValues.addAll(parseReading(Arrays.copyOfRange(input, i*bytesPerValue, (i+1)*bytesPerValue ), timestamp + i * timeBetweenValues));
        }
        return dataValues;
    }
    protected abstract List<GenericData> parseReading(byte[] data, long timestamp);

}
