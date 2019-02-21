package edu.ncsu.csc.assist.data.handling;

import android.provider.ContactsContract;

import edu.ncsu.csc.assist.data.cloud.DataStorer;
import edu.ncsu.csc.assist.data.objects.DataType;
import edu.ncsu.csc.assist.data.objects.PpgData;

public class ChestPpgHandler extends Handler {

    private static final int BYTES_PER_VALUE = 2;
    private static final int NUMBER_OF_VALUES = 4;
    private static final int MILLIS_BETWEEN_VALUES = 5;

    public ChestPpgHandler(DataStorer rawDataBuffer){
        super(BYTES_PER_VALUE, NUMBER_OF_VALUES, rawDataBuffer);
    }
    @Override
    public void handle(byte[] buffer, long timestamp) {
        for (int i = 0; i < getTotalByteSize(); i += getBytesPerValue()) {
            int reading = getIntFromBytes(buffer[i], buffer[i + 1]);
            PpgData dataPoint = new PpgData(DataType.CHEST_PPG, reading, timestamp + i*MILLIS_BETWEEN_VALUES);
            sendRawData(dataPoint);
        }
        //send processed data
    }
}
