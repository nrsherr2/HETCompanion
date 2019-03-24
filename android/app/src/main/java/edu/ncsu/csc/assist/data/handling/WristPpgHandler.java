package edu.ncsu.csc.assist.data.handling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.ncsu.csc.assist.data.cloud.DataStorer;
import edu.ncsu.csc.assist.data.objects.DataType;
import edu.ncsu.csc.assist.data.objects.GenericData;

public class WristPpgHandler extends Handler {

    private static final int BYTES_PER_VALUE = 2;
    private static final int NUMBER_OF_VALUES = 2;
    private static final int MILLIS_BETWEEN_VALUES = 10;

    public WristPpgHandler(DataStorer rawDataBuffer){
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
        int reading = getIntFromBytes(data[0], data[1]);
        GenericData dataPoint = new GenericData(DataType.WRIST_PPG, reading, timestamp);
        return Collections.singletonList(dataPoint);
    }
}
