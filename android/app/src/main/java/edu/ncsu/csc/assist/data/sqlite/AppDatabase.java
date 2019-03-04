package edu.ncsu.csc.assist.data.sqlite;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import edu.ncsu.csc.assist.data.sqlite.access.ConfigOptionDao;
import edu.ncsu.csc.assist.data.sqlite.access.RawDataPointDao;
import edu.ncsu.csc.assist.data.sqlite.entities.ConfigOption;
import edu.ncsu.csc.assist.data.sqlite.entities.RawDataPoint;

@Database(entities = {RawDataPoint.class, ConfigOption.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract RawDataPointDao rawDataPointDao();

    public abstract ConfigOptionDao configOptionDao();
} 
