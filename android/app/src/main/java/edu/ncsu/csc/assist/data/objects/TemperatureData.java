package edu.ncsu.csc.assist.data.objects;

public class TemperatureData extends GenericData{

    public TemperatureData(int reading, long timestamp) {
        super(DataType.WRIST_TEMPERATURE, reading, timestamp);
    }
}
