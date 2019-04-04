package edu.ncsu.csc.assist.data.device;

import org.junit.Test;

import java.util.Random;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import edu.ncsu.csc.assist.data.cloud.DataStorer;
import edu.ncsu.csc.assist.data.cloud.DataUploader;
import edu.ncsu.csc.assist.data.cloud.ProcessedDataStorer;
import edu.ncsu.csc.assist.data.sqlite.AppDatabase;
import edu.ncsu.csc.assist.data.sqlite.entities.ConfigOption;

public class DataDistributorTest {

    @Test
    public void testDataDistributor() throws Exception{
        AppDatabase db = Room.databaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase.class, "ASSIST").build();
        db.clearAllTables();
        db.configOptionDao().insert(new ConfigOption("config_user_id", "0"));
        db.configOptionDao().insert(new ConfigOption("config_het_version", "0.1"));
        db.configOptionDao().insert(new ConfigOption("user_0_ts_delta", "0"));

        final int CHEST_STREAM_ONE_BYTES = 20;
        final int CHEST_STREAM_TWO_BYTES = 12;
        final int WRIST_STREAM_ONE_BYTES = 16;
        final int WRIST_STREAM_TWO_BYTES = 12;
        final int NUM_PACKAGES = 1;
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
        }

        storer.flush();

    }
}
