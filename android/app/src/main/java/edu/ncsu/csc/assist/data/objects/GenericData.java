package edu.ncsu.csc.assist.data.objects;

import java.util.Locale;
import java.util.Objects;

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

    @Override
    public String toString() {
        return String.format(Locale.US, "GenericData[%s, %d, %s]", type, timestamp, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        GenericData that = (GenericData) o;
        return value == that.value &&
                timestamp == that.timestamp &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value, timestamp);
    }
}
