package edu.ncsu.csc.assist.data.device;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Collection;

import edu.ncsu.csc.assist.data.cloud.DataStorer;
import edu.ncsu.csc.assist.data.objects.GenericData;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;

public class DataDistributorTest {

    @Mock
    DataStorer dataStore;

    @Before
    public void setUp() throws Exception {
        // Do nothing when the datastore is called to save the data
        Mockito.doNothing().when(dataStore).save(ArgumentMatchers.<GenericData>any());
        Mockito.doNothing().when(dataStore).save(ArgumentMatchers.<Collection<GenericData>>any());

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void distributeChestData() {
    }

    @Test
    public void distributeWristData() {
    }
}