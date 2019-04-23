package edu.ncsu.csc.assist.data.sqlite.entities;

import java.util.Locale;
import java.util.Objects;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "processed_data")
public class ProcessedDataPoint {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public long id;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "value")
    public double value;

    private ProcessedDataPoint() {

    }

    public ProcessedDataPoint(String type, long timestamp, double value) {
        this.type = type;
        this.timestamp = timestamp;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getValue() {
        return value;
    }

    public void setTimestamp(long newTimestamp) {
        this.timestamp = newTimestamp;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "ProcessedDataPoint[%d, %s, %d, %s]", id, type, timestamp, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessedDataPoint dataPoint = (ProcessedDataPoint) o;
        return id == dataPoint.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
