package edu.ncsu.csc.assist.data.sqlite;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.runner.AndroidJUnit4;
import edu.ncsu.csc.assist.data.objects.DataType;
import edu.ncsu.csc.assist.data.sqlite.access.RawDataPointDao;
import edu.ncsu.csc.assist.data.sqlite.entities.RawDataPoint;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class DatabaseRawDataTest {

    private RawDataPointDao rawDataPointDao;
    private AppDatabase db;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        rawDataPointDao = db.rawDataPointDao();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void generateData() throws Exception {
        final int RAW_DATA_COUNT = 100;
        for (int i = 0; i < RAW_DATA_COUNT; i++) {
            long[] id = rawDataPointDao.insert(new RawDataPoint(DataType.values()[i % DataType.values().length].getId(), System.currentTimeMillis(), ThreadLocalRandom.current().nextInt()));
        }

        assertThat(rawDataPointDao.getAll().size(), equalTo(RAW_DATA_COUNT));

        // Valid IDs
        assertNotNull(rawDataPointDao.getById(1));
        assertNotNull(rawDataPointDao.getById(3));
        assertNotNull(rawDataPointDao.getById(9));

        // Invalid IDs
        assertNull(rawDataPointDao.getById(-1));
        assertNull(rawDataPointDao.getById(0));
        assertNull(rawDataPointDao.getById(RAW_DATA_COUNT + 1));

        // These exist based on previous test
        RawDataPoint id1 = rawDataPointDao.getById(1);
        RawDataPoint id3 = rawDataPointDao.getById(3);
        RawDataPoint id9 = rawDataPointDao.getById(9);


        assertThat(id1.getId(), equalTo(1L));
        assertThat(id3.getId(), equalTo(3L));
        assertThat(id9.getId(), equalTo(9L));
        assertThat(id1.getType(), equalTo(DataType.values()[0].getId()));
        assertThat(id3.getType(), equalTo(DataType.values()[2].getId()));
        assertThat(id9.getType(), equalTo(DataType.values()[8].getId()));
    }
}
