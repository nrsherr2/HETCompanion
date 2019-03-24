package edu.ncsu.csc.assist.data.handling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.ncsu.csc.assist.data.cloud.DataStorer;
import edu.ncsu.csc.assist.data.objects.DataType;
import edu.ncsu.csc.assist.data.objects.GenericData;

public class WristInertialHandler extends Handler {

    private static final int BYTES_PER_VALUE = 6;
    private static final int NUMBER_OF_VALUES = 2;
    private static final int MILLIS_BETWEEN_VALUES = 10;

    public WristInertialHandler(DataStorer rawDataBuffer){
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
        int xReading = getIntFromBytes(data[0], data[1]);
        GenericData xDataPoint = new GenericData(DataType.WRIST_INERTIA_X, xReading, timestamp);

        int yReading = getIntFromBytes(data[2], data[3]);
        GenericData yDataPoint = new GenericData(DataType.WRIST_INERTIA_Y, yReading, timestamp);

        int zReading = getIntFromBytes(data[4], data[5]);
        GenericData zDataPoint = new GenericData(DataType.WRIST_INERTIA_Z, zReading, timestamp);

        return  Arrays.asList(xDataPoint,yDataPoint,zDataPoint);
    }
}
