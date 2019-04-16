package edu.ncsu.csc.assist.data.sqlite;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.runner.AndroidJUnit4;
import edu.ncsu.csc.assist.data.objects.ProcessedDataType;
import edu.ncsu.csc.assist.data.objects.SummarizedData;
import edu.ncsu.csc.assist.data.sqlite.access.ProcessedDataPointDao;
import edu.ncsu.csc.assist.data.sqlite.entities.ProcessedDataPoint;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class DatabaseProcessedDataTest {

    private ProcessedDataPointDao processedDataPointDao;
    private AppDatabase db;

    final int DATA_COUNT = 600;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        processedDataPointDao = db.processedDataPointDao();


        long ts = 0;
        for (int i = 0; i < DATA_COUNT; i++) {
            processedDataPointDao.insert(new ProcessedDataPoint(ProcessedDataType.HEARTRATE, ts, i));
            ts += 1000;
        }
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void testInsert() throws Exception {
        assertThat(processedDataPointDao.getAll(ProcessedDataType.HEARTRATE).size(), equalTo(DATA_COUNT));
    }

    @Test
    public void testSummary() throws Exception {
        assertThat(processedDataPointDao.getAll(ProcessedDataType.HEARTRATE).size(), equalTo(600));

        // Test Live summary
        List<SummarizedData> live = processedDataPointDao.querySummarizedData(ProcessedDataType.HEARTRATE, ProcessedDataPointDao.LIVE);
        assertThat(live.size(), equalTo(600));

        // Test Minutely summary
        List<SummarizedData> minutely = processedDataPointDao.querySummarizedData(ProcessedDataType.HEARTRATE, ProcessedDataPointDao.MINUTELY);
        System.out.println(Arrays.toString(minutely.toArray()));
        assertThat(minutely.size(), equalTo(10));
    }
}
