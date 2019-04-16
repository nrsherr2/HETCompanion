package edu.ncsu.csc.assist.data.objects;

import androidx.room.ColumnInfo;

public class SummarizedData {
    @ColumnInfo(name = "interval")
    public long interval;

    @ColumnInfo(name = "value")
    public double value;

    @Override
    public String toString() {
        return "SummarizedData{" +
                "interval='" + interval + '\'' +
                ", value=" + value +
                '}';
    }
}
