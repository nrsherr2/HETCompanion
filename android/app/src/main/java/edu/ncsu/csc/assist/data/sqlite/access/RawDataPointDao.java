package edu.ncsu.csc.assist.data.sqlite.access;

import java.util.Collection;
import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import edu.ncsu.csc.assist.data.sqlite.entities.RawDataPoint;

@Dao
public interface RawDataPointDao {

    @Query("SELECT * FROM raw_data")
    List<RawDataPoint> getAll();

    @Query("SELECT * FROM raw_data WHERE id = :id")
    RawDataPoint getById(long id);

    @Insert
    long[] insert(RawDataPoint... dataPoints);

    @Insert
    long[] insertAll(Collection<RawDataPoint> dataPoints);

    @Delete
    int delete(RawDataPoint dataPoint);

    @Query("DELETE FROM raw_data WHERE id = :id")
    int deleteById(long id);
} 
