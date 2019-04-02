package edu.ncsu.csc.assist.data.objects;

import java.util.Locale;
import java.util.Objects;

public class ProcessedData {

    private String type;
    private double value;
    private long timestamp;

    public ProcessedData(String type, double value, long timestamp) {
        this.type = type;
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getType() {
        return this.type;
    }

    public double getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "ProcessedData[%s, %f, %s]", type, timestamp, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        ProcessedData that = (ProcessedData) o;
        return value == that.value &&
                timestamp == that.timestamp &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value, timestamp);
    }
}
