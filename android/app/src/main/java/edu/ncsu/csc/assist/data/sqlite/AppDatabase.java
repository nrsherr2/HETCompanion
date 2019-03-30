package edu.ncsu.csc.assist.data.sqlite;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import edu.ncsu.csc.assist.data.sqlite.access.ConfigOptionDao;
import edu.ncsu.csc.assist.data.sqlite.access.ProcessedDataPointDao;
import edu.ncsu.csc.assist.data.sqlite.access.RawDataPointDao;
import edu.ncsu.csc.assist.data.sqlite.entities.ConfigOption;
import edu.ncsu.csc.assist.data.sqlite.entities.ProcessedDataPoint;
import edu.ncsu.csc.assist.data.sqlite.entities.RawDataPoint;

@Database(entities = {RawDataPoint.class, ConfigOption.class, ProcessedDataPoint.class}, version = 4)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "ASSIST";
    private static AppDatabase INSTANCE;

    public abstract RawDataPointDao rawDataPointDao();

    public abstract ConfigOptionDao configOptionDao();

    public abstract ProcessedDataPointDao processedDataPointDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DATABASE_NAME)
                            .allowMainThreadQueries()   //TODO remove allowMainThreadQueries
                            .fallbackToDestructiveMigration()   // If the database is an old schema, delete old data. This will only happen during development
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
