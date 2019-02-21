package edu.ncsu.csc.assist.data.handling;

import edu.ncsu.csc.assist.data.cloud.DataStorer;
import edu.ncsu.csc.assist.data.objects.DataType;
import edu.ncsu.csc.assist.data.objects.InertialData;

public class WristInertialHandler extends Handler {

    private static final int BYTES_PER_VALUE = 6;
    private static final int NUMBER_OF_VALUES = 2;
    private static final int MILLIS_BETWEEN_VALUES = 10;

    public WristInertialHandler(DataStorer rawDataBuffer){
        super(BYTES_PER_VALUE, NUMBER_OF_VALUES, rawDataBuffer);
    }

    @Override
    public void handle(byte[] buffer, long timestamp) {
        for (int i = 0; i < getTotalByteSize(); i += getBytesPerValue()) {
            int xReading = getIntFromBytes(buffer[i], buffer[i + 1]);
            InertialData xDataPoint = new InertialData(DataType.WRIST_INERTIA_X, xReading, timestamp + i*MILLIS_BETWEEN_VALUES);
            sendRawData(xDataPoint);

            int yReading = getIntFromBytes(buffer[i + 2], buffer[i + 3]);
            InertialData yDataPoint = new InertialData(DataType.WRIST_INERTIA_Y, yReading, timestamp + i*MILLIS_BETWEEN_VALUES);
            sendRawData(yDataPoint);

            int zReading = getIntFromBytes(buffer[i + 4], buffer[i + 5]);
            InertialData zDataPoint = new InertialData(DataType.WRIST_INERTIA_Z, zReading, timestamp + i*MILLIS_BETWEEN_VALUES);
            sendRawData(zDataPoint);
        }
        //send processed data
    }
}
