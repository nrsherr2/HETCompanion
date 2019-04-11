package edu.ncsu.csc.assist.data.sqlite.access;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import edu.ncsu.csc.assist.data.objects.SummarizedData;
import edu.ncsu.csc.assist.data.sqlite.entities.ProcessedDataPoint;

@Dao
public interface ProcessedDataPointDao {

    // Seconds in intervals; used for querySummarizedData
    int LIVE = 1;
    int MINUTELY = 60;
    int HOURLY = 3600;
    int DAILY = 86400;
    int WEEKLY = 604800;
    int MONTHLY = 2419200;

    @Query("SELECT * FROM processed_data WHERE type = :type")
    List<ProcessedDataPoint> getAll(String type);

    @Query("SELECT * FROM processed_data WHERE type = :type ORDER BY timestamp DESC LIMIT 1")
    LiveData<ProcessedDataPoint> getMostRecent(String type);

    @Insert
    long insert(ProcessedDataPoint dataPoint);

    @Insert
    long[] insertAll(List<ProcessedDataPoint> dataPoint);

    @Query("SELECT (cast(timestamp / (:period * 1000) as int) * (:period * 1000)) AS interval,\n" +
            "  AVG(value) value\n" +
            "FROM processed_data\n" +
            "WHERE type=:type\n"+
            "GROUP BY interval\n" +
            "ORDER BY interval")
    List<SummarizedData> querySummarizedData(String type, int period);
} 
