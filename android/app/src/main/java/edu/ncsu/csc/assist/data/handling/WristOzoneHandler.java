package edu.ncsu.csc.assist.data.handling;

import edu.ncsu.csc.assist.data.cloud.DataStorer;
import edu.ncsu.csc.assist.data.objects.DataType;
import edu.ncsu.csc.assist.data.objects.OzoneData;

public class WristOzoneHandler extends Handler {

    private static final int BYTES_PER_VALUE = 8;
    private static final int NUMBER_OF_VALUES = 1;
    private static final int MILLIS_BETWEEN_VALUES = 20;

    public WristOzoneHandler(DataStorer rawDataBuffer){
        super(BYTES_PER_VALUE, NUMBER_OF_VALUES, rawDataBuffer);
    }

    @Override
    public void handle(byte[] buffer, long timestamp) {
        for (int i = 0; i < getTotalByteSize(); i += getBytesPerValue()) {
            int ozReading = getIntFromBytes(buffer[i], buffer[i + 1]);
            OzoneData ozDataPoint = new OzoneData(DataType.WRIST_OZ, ozReading, timestamp + i*MILLIS_BETWEEN_VALUES);
            sendRawData(ozDataPoint);

            int pozReading = getIntFromBytes(buffer[i + 2], buffer[i + 3]);
            OzoneData pozDataPoint = new OzoneData(DataType.WRIST_POZ, pozReading, timestamp + i*MILLIS_BETWEEN_VALUES);
            sendRawData(pozDataPoint);

            int rozReading = getIntFromBytes(buffer[i + 4], buffer[i + 5]);
            OzoneData rozDataPoint = new OzoneData(DataType.WRIST_ROZ, rozReading, timestamp + i*MILLIS_BETWEEN_VALUES);
            sendRawData(rozDataPoint);

            int mozReading = getIntFromBytes(buffer[i + 6], buffer[i + 7]);
            OzoneData mozDataPoint = new OzoneData(DataType.WRIST_MOZ, mozReading, timestamp + i*MILLIS_BETWEEN_VALUES);
            sendRawData(mozDataPoint);
        }
        //send processed data
    }
}
