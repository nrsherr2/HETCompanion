package edu.ncsu.csc.assist.data.device;

import android.content.Context;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import edu.ncsu.csc.assist.data.cloud.DataStorer;
import edu.ncsu.csc.assist.data.cloud.DataUploader;
import edu.ncsu.csc.assist.data.cloud.ProcessedDataStorer;
import edu.ncsu.csc.assist.data.objects.DataType;
import edu.ncsu.csc.assist.data.objects.GenericData;
import edu.ncsu.csc.assist.data.sqlite.AppDatabase;
import edu.ncsu.csc.assist.data.sqlite.access.RawDataPointDao;
import edu.ncsu.csc.assist.data.sqlite.entities.ConfigOption;
import edu.ncsu.csc.assist.data.sqlite.entities.RawDataPoint;



public class DataDistributorTest {

    private RawDataPointDao rawDataPointDao;
    private AppDatabase db;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        db.clearAllTables();
        rawDataPointDao = db.rawDataPointDao();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void testRawDataDistribution() throws Exception{
        AppDatabase db = Room.databaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase.class, "ASSIST").build();
        db.clearAllTables();
        db.configOptionDao().insert(new ConfigOption("config_user_id", "0"));
        db.configOptionDao().insert(new ConfigOption("config_het_version", "0.1"));
        db.configOptionDao().insert(new ConfigOption("user_0_ts_delta", "0"));

        final int CHEST_STREAM_ONE_BYTES = 20;
        final int CHEST_STREAM_TWO_BYTES = 12;
        final int WRIST_STREAM_ONE_BYTES = 16;
        final int WRIST_STREAM_TWO_BYTES = 12;
        final int NUM_PACKAGES = 2;
        final long timestampStart = 5000;
        final long timestampDelta = 20;
        Random gen = new Random();
        // Create mocked google sign in account for data uploader
        //GoogleSignInAccount googleAccount = Mockito.mock(GoogleSignInAccount.class);
        //Mockito.when(googleAccount.getIdToken()).thenReturn("mockedToken");

        DataUploader uploader = null; //new DataUploader(ApplicationProvider.getApplicationContext(), googleAccount);
        DataStorer storer = new DataStorer(ApplicationProvider.getApplicationContext());
        ProcessedDataStorer processedStorer = new ProcessedDataStorer(ApplicationProvider.getApplicationContext());
        DataDistributor distributor = new DataDistributor(storer, processedStorer);

        byte[] chestStreamOneData = new byte[CHEST_STREAM_ONE_BYTES];
        byte[] chestStreamTwoData = new byte[CHEST_STREAM_TWO_BYTES];
        byte[] wristStreamOneData = new byte[WRIST_STREAM_ONE_BYTES];
        byte[] wristStreamTwoData = new byte[WRIST_STREAM_TWO_BYTES];

        for(int i = 0; i < NUM_PACKAGES; i++){
            gen.nextBytes(chestStreamOneData);
            gen.nextBytes(chestStreamTwoData);
            gen.nextBytes(wristStreamOneData);
            gen.nextBytes(wristStreamTwoData);
            distributor.distributeChestStreamOne(chestStreamOneData, timestampStart + i * timestampDelta);
            distributor.distributeChestStreamTwo(chestStreamTwoData, timestampStart + i * timestampDelta);
            distributor.distributeWristStreamOne(wristStreamOneData, timestampStart + i * timestampDelta);
            distributor.distributeWristStreamTwo(wristStreamTwoData, timestampStart + i * timestampDelta);
            storer.flush();
            List<RawDataPoint> data = db.rawDataPointDao().getAll();
            assertEquals(28, data.size());  //checks the total number of readings

            int iterator;

            //TESTING CHEST STREAM ONE
            iterator = 0;
            //Testing ECG Readings.
            final int CHEST_ECG_READINGS = 4;
            final int CHEST_ECG_READING_SIZE = 3;
            iterator = testParse(iterator, chestStreamOneData, CHEST_ECG_READINGS, CHEST_ECG_READING_SIZE, timestampStart+i*timestampDelta, timestampDelta, DataType.CHEST_ECG, data);

            //Testing Chest ppg Readings.
            final int CHEST_PPG_READINGS = 4;
            final int CHEST_PPG_READING_SIZE = 2;
            iterator = testParse(iterator, chestStreamOneData, CHEST_PPG_READINGS, CHEST_PPG_READING_SIZE, timestampStart+i*timestampDelta, timestampDelta, DataType.CHEST_PPG, data);

            //TESTING CHEST STREAM TWO
            iterator = 0;
            //Testing Chest inertial Readings : (x1, y1, z1, x2, y2, z2)
            // - one reading each, two bytes per reading
            // - (x/y/z)2 is taken 10 ms later than (x/y/z)1
            iterator = testParse(iterator, chestStreamTwoData, 1, 2, timestampStart+i*timestampDelta, timestampDelta, DataType.CHEST_INERTIA_X, data);
            iterator = testParse(iterator, chestStreamTwoData, 1, 2, timestampStart+i*timestampDelta, timestampDelta, DataType.CHEST_INERTIA_Y, data);
            iterator = testParse(iterator, chestStreamTwoData, 1, 2, timestampStart+i*timestampDelta, timestampDelta, DataType.CHEST_INERTIA_Z, data);
            iterator = testParse(iterator, chestStreamTwoData, 1, 2, timestampStart+i*timestampDelta + 10, timestampDelta, DataType.CHEST_INERTIA_X, data);
            iterator = testParse(iterator, chestStreamTwoData, 1, 2, timestampStart+i*timestampDelta + 10, timestampDelta, DataType.CHEST_INERTIA_Y, data);
            iterator = testParse(iterator, chestStreamTwoData, 1, 2, timestampStart+i*timestampDelta + 10, timestampDelta, DataType.CHEST_INERTIA_Z, data);


            //TESTING WRIST STREAM ONE
            iterator = 0;
            //Testing Wrist inertial Readings : (x1, y1, z1, x2, y2, z2)
            // - one reading each, two bytes per reading
            // - (x/y/z)2 is taken 10 ms later than (x/y/z)1
            iterator = testParse(iterator, wristStreamOneData, 1, 2, timestampStart+i*timestampDelta, timestampDelta, DataType.WRIST_INERTIA_X, data);
            iterator = testParse(iterator, wristStreamOneData, 1, 2, timestampStart+i*timestampDelta, timestampDelta, DataType.WRIST_INERTIA_Y, data);
            iterator = testParse(iterator, wristStreamOneData, 1, 2, timestampStart+i*timestampDelta, timestampDelta, DataType.WRIST_INERTIA_Z, data);
            iterator = testParse(iterator, wristStreamOneData, 1, 2, timestampStart+i*timestampDelta + 10, timestampDelta, DataType.WRIST_INERTIA_X, data);
            iterator = testParse(iterator, wristStreamOneData, 1, 2, timestampStart+i*timestampDelta + 10, timestampDelta, DataType.WRIST_INERTIA_Y, data);
            iterator = testParse(iterator, wristStreamOneData, 1, 2, timestampStart+i*timestampDelta + 10, timestampDelta, DataType.WRIST_INERTIA_Z, data);

            //Testing Wrist PPG Readings
            final int WRIST_PPG_READINGS = 2;
            final int WRIST_PPG_READING_SIZE = 2;
            iterator = testParse(iterator, wristStreamOneData, WRIST_PPG_READINGS, WRIST_PPG_READING_SIZE, timestampStart+i*timestampDelta, timestampDelta, DataType.WRIST_PPG, data);

            //TESTING WRIST STREAM TWO
            iterator = 0;
            //Testing Ozone readings : (oz, poz, roz, moz)
            // - one reading each, two bytes per reading
            iterator = testParse(iterator, wristStreamTwoData, 1, 2, timestampStart+i*timestampDelta, timestampDelta, DataType.WRIST_OZ, data);
            iterator = testParse(iterator, wristStreamTwoData, 1, 2, timestampStart+i*timestampDelta, timestampDelta, DataType.WRIST_POZ, data);
            iterator = testParse(iterator, wristStreamTwoData, 1, 2, timestampStart+i*timestampDelta, timestampDelta, DataType.WRIST_ROZ, data);
            iterator = testParse(iterator, wristStreamTwoData, 1, 2, timestampStart+i*timestampDelta, timestampDelta, DataType.WRIST_MOZ, data);

            //Testing Environmental readings : (temperature, humidity)
            // - one reading each, two bytes per reading
            iterator = testParse(iterator, wristStreamTwoData, 1, 2, timestampStart+i*timestampDelta, timestampDelta, DataType.WRIST_TEMPERATURE, data);
            iterator = testParse(iterator, wristStreamTwoData, 1, 2, timestampStart+i*timestampDelta, timestampDelta, DataType.WRIST_HUMIDITY, data);

            db.clearAllTables();
        }
    }

    private int testParse(int iterator, byte[] inputData, int numReadings, int readingSize, long startT, long deltaT, String dataType, List<RawDataPoint> data){
        byte[] reading = new byte[readingSize];
        int perReadingDeltaT = (int) deltaT/numReadings;
        for(int readingCount = 0; readingCount < numReadings; readingCount++){
            assertEquals(dataType, data.get(0).getType());
            assertEquals(startT + readingCount * perReadingDeltaT, data.get(0).getTimestamp());
            for(int i = 0; i < readingSize; i++){
                reading[i] = inputData[iterator++];
            }
            assertEquals(constructFromBytes(reading), data.get(0).getValue());
            data.remove(0);
        }
        return iterator;
    }

    private int constructFromBytes(byte... bytes){
        final int bitMask = 0x000000FF;
        int result = (int)bytes[0];
        for(int i = 1; i < bytes.length; i++){
            result = (result << 8) | (bytes[i] & bitMask);
        }
        return result;
    }
}
