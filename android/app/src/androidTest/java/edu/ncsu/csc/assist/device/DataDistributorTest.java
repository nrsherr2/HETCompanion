package edu.ncsu.csc.assist.device;

import org.junit.Test;

import java.util.Random;

import androidx.test.core.app.ApplicationProvider;
import edu.ncsu.csc.assist.data.cloud.DataStorer;
import edu.ncsu.csc.assist.data.device.DataDistributor;

public class DataDistributorTest {

    @Test
    public void testDataDistributor(){
        final int CHEST_BYTES = 32;
        final int WRIST_BYTES = 28;
        final int NUM_PACKAGES = 10;
        final long timestampStart = 5000;
        final long timestampDelta = 20;
        Random gen = new Random();
        DataDistributor distributor = new DataDistributor(new DataStorer(ApplicationProvider.getApplicationContext()));

        byte[] chestData = new byte[CHEST_BYTES];
        byte[] wristData = new byte[WRIST_BYTES];

        for(int i = 0; i < NUM_PACKAGES; i++){
            gen.nextBytes(chestData);
            gen.nextBytes(wristData);
            distributor.distributeChestData(chestData, timestampStart + i*timestampDelta);
            distributor.distributeWristData(wristData, timestampStart + i*timestampDelta);
        }
    }
}
