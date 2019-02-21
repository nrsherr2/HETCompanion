package edu.ncsu.csc.assist.data.objects;

public abstract class GenericData {

    private DataType type;
    private int value;
    private long timestamp;

    public GenericData(DataType type, int value, long timestamp) {
        this.type = type;
        this.value = value;
        this.timestamp = timestamp;
    }

    public DataType getType() {
        return this.type;
    }

    public int getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
