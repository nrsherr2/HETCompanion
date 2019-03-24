package edu.ncsu.csc.assist.data.cloud;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import edu.ncsu.csc.assist.data.objects.DataType;
import edu.ncsu.csc.assist.data.sqlite.entities.RawDataPoint;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class JsonUtilTest {

    private static final int RAW_DATA_COUNT = 100;


    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testFormatJson() throws Exception {
        final String expectedJson = "{\"user_id\":\"0\",\"chest_ecg\":{\"initial_timestamp\":0,\"data\":[10,11,12,13,14],\"delta\":[5,5,5,5]},\"het_version\":\"0.1\",\"chest_ppg\":{\"initial_timestamp\":0,\"data\":[100,101,102,103,104],\"delta\":[6,6,6,6]},\"chest_inertia_x\":{\"initial_timestamp\":0,\"data\":[1000,1001,1002,1003,1004],\"delta\":[7,7,7,7]}}";

        List<RawDataPoint> data = new ArrayList<>(25);

        // Populate data list
        for (int i = 0; i < 5; i++) {
            data.add(new RawDataPoint(DataType.CHEST_ECG, i * 5, 10 + i));
        }
        for (int i = 0; i < 5; i++) {
            data.add(new RawDataPoint(DataType.CHEST_PPG, i * 6, 100 + i));
        }
        for (int i = 0; i < 5; i++) {
            data.add(new RawDataPoint(DataType.CHEST_INERTIA_X, i * 7, 1000 + i));
        }

        assertThat(JsonUtil.formatJson("0", "0.1", data).toString(0), equalTo(expectedJson));
    }

    @Test
    public void testFormatJson_NoData() throws Exception {
        assertThat(JsonUtil.formatJson("0", "0.1", new ArrayList<RawDataPoint>()).toString(0), equalTo("{\"user_id\":\"0\",\"het_version\":\"0.1\"}"));
    }
}