package edu.ncsu.csc.assist.data.objects;

public class EcgData extends GenericData{

    public EcgData(int reading, long timestamp) {
        super(DataType.CHEST_ECG, reading, timestamp);
    }
}
