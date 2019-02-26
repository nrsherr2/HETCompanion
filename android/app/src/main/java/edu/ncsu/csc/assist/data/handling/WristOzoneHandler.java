package edu.ncsu.csc.assist.data.handling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.ncsu.csc.assist.data.cloud.DataStorer;
import edu.ncsu.csc.assist.data.objects.DataType;
import edu.ncsu.csc.assist.data.objects.GenericData;

public class WristOzoneHandler extends Handler {

    private static final int BYTES_PER_VALUE = 8;
    private static final int NUMBER_OF_VALUES = 1;
    private static final int MILLIS_BETWEEN_VALUES = 20;

    public WristOzoneHandler(DataStorer rawDataBuffer){
        super(BYTES_PER_VALUE, NUMBER_OF_VALUES, MILLIS_BETWEEN_VALUES, rawDataBuffer);
    }

    @Override
    public void handle(byte[] buffer, long timestamp) {
        List<GenericData> dataValues = parseInput(buffer, timestamp);
        sendRawData(dataValues);

        //send processed data
    }

    @Override
    protected List<GenericData> parseReading(byte[] data, long timestamp) {
        int ozReading = getIntFromBytes(data[0], data[1]);
        GenericData ozDataPoint = new GenericData(DataType.WRIST_OZ, ozReading, timestamp);

        int pozReading = getIntFromBytes(data[2], data[3]);
        GenericData pozDataPoint = new GenericData(DataType.WRIST_POZ, pozReading, timestamp);

        int rozReading = getIntFromBytes(data[4], data[5]);
        GenericData rozDataPoint = new GenericData(DataType.WRIST_ROZ, rozReading, timestamp);

        int mozReading = getIntFromBytes(data[6], data[7]);
        GenericData mozDataPoint = new GenericData(DataType.WRIST_MOZ, mozReading, timestamp);

        return Arrays.asList(ozDataPoint, pozDataPoint, rozDataPoint, mozDataPoint);
    }
}
