package edu.ncsu.csc.assist.data.objects;

public class HumidityData extends GenericData{

    public HumidityData(int reading, long timestamp) {
        super(DataType.WRIST_HUMIDITY, reading, timestamp);
    }
}
