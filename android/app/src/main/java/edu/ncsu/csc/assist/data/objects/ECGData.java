package edu.ncsu.csc.assist.data.objects;

public class ECGData extends GenericData{

    public ECGData(int reading, long timestamp) {
        super(DataType.CHEST_ECG, reading, timestamp);
    }
}
