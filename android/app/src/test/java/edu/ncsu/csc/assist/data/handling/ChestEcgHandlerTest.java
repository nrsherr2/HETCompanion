package edu.ncsu.csc.assist.data.handling;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import edu.ncsu.csc.assist.data.objects.DataType;
import edu.ncsu.csc.assist.data.objects.GenericData;

public class ChestEcgHandlerTest {

    /**
     * Tests the parseReading function of the handler.
     * This test is responsible for receive bytes of a single instrument reading and returning one
     * or more GenericData objects created from that reading
     */
    @Test
    public void parseReading() {
        ChestEcgHandler ecgHandler = new ChestEcgHandler(null, null);
        long testTimestamp = 5000;
        byte[] testEcgData = {
                0x11, 0x22, 0x33,
        };

        List<GenericData> dataValues = ecgHandler.parseReading(testEcgData, testTimestamp);
        Assert.assertEquals(1, dataValues.size());

        Assert.assertEquals(0x00112233,dataValues.get(0).getValue());
        Assert.assertEquals(testTimestamp,dataValues.get(0).getTimestamp());
        Assert.assertEquals(DataType.CHEST_ECG, dataValues.get(0).getType());
    }
}