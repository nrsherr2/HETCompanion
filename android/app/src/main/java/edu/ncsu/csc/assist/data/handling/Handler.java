package edu.ncsu.csc.assist.data.handling;

import android.provider.ContactsContract;

import edu.ncsu.csc.assist.data.cloud.DataStorer;
import edu.ncsu.csc.assist.data.objects.GenericData;

public abstract class Handler {

    protected int bytesPerValue;

    protected int numberOfValues;

    private DataStorer rawDataBuffer;

    public Handler(int bytesPerValue, int numberOfValues, DataStorer rawDataBuffer) {
        this.bytesPerValue = bytesPerValue;
        this.numberOfValues = numberOfValues;
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

}
