package edu.ncsu.csc.assist.data.objects;

public class GenericData {

    private String type;
    private int value;
    private long timestamp;

    public GenericData(String type, int value, long timestamp) {
        this.type = type;
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getType() {
        return this.type;
    }

    public int getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
