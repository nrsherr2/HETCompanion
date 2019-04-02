package edu.ncsu.csc.assist.data.sqlite.access;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import edu.ncsu.csc.assist.data.sqlite.entities.ProcessedDataPoint;

@Dao
public interface ProcessedDataPointDao {

    @Query("SELECT * FROM processed_data WHERE type = :type ORDER BY timestamp DESC LIMIT 1")
    LiveData<ProcessedDataPoint> getMostRecent(String type);

    @Insert
    long insert(ProcessedDataPoint dataPoint);

    @Insert
    long[] insertAll(List<ProcessedDataPoint> dataPoint);
} 
