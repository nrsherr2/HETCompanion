package edu.ncsu.csc.assist.data.sqlite.entities;

import java.util.Locale;
import java.util.Objects;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "raw_data")
public class RawDataPoint {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public long id;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "value")
    public int value;

    private RawDataPoint() {

    }

    public RawDataPoint(String type, long timestamp, int value) {
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

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "RawDataPoint[%d, %s, %d, %s]", id, type, timestamp, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RawDataPoint dataPoint = (RawDataPoint) o;
        return id == dataPoint.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
