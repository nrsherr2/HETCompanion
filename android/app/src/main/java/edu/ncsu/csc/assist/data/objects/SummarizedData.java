package edu.ncsu.csc.assist.data.objects;

import androidx.room.ColumnInfo;

public class SummarizedData {
    @ColumnInfo(name = "interval")
    public String interval;

    @ColumnInfo(name = "value")
    public double value;
}
