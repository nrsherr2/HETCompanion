package edu.ncsu.csc.assist.data.sqlite;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import edu.ncsu.csc.assist.data.sqlite.access.RawDataPointDao;
import edu.ncsu.csc.assist.data.sqlite.entities.RawDataPoint;

@Database(entities = {RawDataPoint.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract RawDataPointDao rawDataPointDao();
} 
