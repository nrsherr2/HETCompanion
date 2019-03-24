package edu.ncsu.csc.assist.data.cloud;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import edu.ncsu.csc.assist.data.objects.DataType;
import edu.ncsu.csc.assist.data.objects.GenericData;
import edu.ncsu.csc.assist.data.sqlite.AppDatabase;
import edu.ncsu.csc.assist.data.sqlite.access.RawDataPointDao;
import edu.ncsu.csc.assist.data.sqlite.entities.RawDataPoint;

import static org.junit.Assert.assertTrue;

public class DataStorerTest {

    private DataStorer dataStorer;

    private RawDataPointDao rawDataPointDao;
    private AppDatabase db;


    @Before
    public void setUp() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        rawDataPointDao = db.rawDataPointDao();
        dataStorer = new DataStorer(context).withDatabase(db).startSaveTask();

        db.clearAllTables();
    }

    @After
    public void tearDown() throws Exception {
        dataStorer.stopSaveTask();
        db.clearAllTables();
    }

    @Test
    public void testSave() throws Exception {
        GenericData data = new GenericData(DataType.CHEST_ECG, 1000, 5000);
        dataStorer.save(data);

        // Wait at least 1 second for the datastorer to dump the queue to the database
        Thread.sleep(2000);

        List<GenericData> retrievedData = new ArrayList<>();
        for (RawDataPoint rawDataPoint : rawDataPointDao.getAll()) {
            retrievedData.add(new GenericData(rawDataPoint.getType(), rawDataPoint.getValue(), rawDataPoint.getTimestamp()));
        }

        assertTrue(retrievedData.contains(data));
    }

    @Test
    public void testSaveAll() throws Exception {
        GenericData data1 = new GenericData(DataType.WRIST_HUMIDITY, 500, 250);
        GenericData data2 = new GenericData(DataType.CHEST_INERTIA_X, 25, 1000);
        GenericData data3 = new GenericData(DataType.CHEST_INERTIA_Y, 20, 1000);
        GenericData data4 = new GenericData(DataType.CHEST_INERTIA_Z, 5, 1000);
        List<GenericData> data = Arrays.asList(data1, data2, data3, data4);
        dataStorer.save(data);

        // Wait at least 1 second for the datastorer to dump the queue to the database
        Thread.sleep(2000);

        List<GenericData> retrievedData = new ArrayList<>();
        for (RawDataPoint rawDataPoint : rawDataPointDao.getAll()) {
            retrievedData.add(new GenericData(rawDataPoint.getType(), rawDataPoint.getValue(), rawDataPoint.getTimestamp()));
        }

        assertTrue(retrievedData.containsAll(data));
    }
}
