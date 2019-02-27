package edu.ncsu.csc.assist.data.handling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.ncsu.csc.assist.data.cloud.DataStorer;
import edu.ncsu.csc.assist.data.objects.DataType;
import edu.ncsu.csc.assist.data.objects.GenericData;

public class WristEnvironmentalHandler extends Handler {

    private static final int BYTES_PER_VALUE = 4;
    private static final int NUMBER_OF_VALUES = 1;
    private static final int MILLIS_BETWEEN_VALUES = 20;

    public WristEnvironmentalHandler(DataStorer rawDataBuffer){
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
        int temperatureReading = getIntFromBytes(data[0], data[1]);
        GenericData temperatureDataPoint = new GenericData(DataType.WRIST_TEMPERATURE, temperatureReading, timestamp);

        int humidReading = getIntFromBytes(data[2], data[3]);
        GenericData humidDataPoint = new GenericData(DataType.WRIST_HUMIDITY, humidReading, timestamp);

        return Arrays.asList(temperatureDataPoint, humidDataPoint);
    }
}
