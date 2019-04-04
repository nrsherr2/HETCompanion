package edu.ncsu.csc.assist.data.handling;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import edu.ncsu.csc.assist.data.objects.DataType;
import edu.ncsu.csc.assist.data.objects.GenericData;

public class WristInertialHandlerTest {

    /**
     * Tests the parseReading function of the handler.
     * This test is responsible for receive bytes of a single instrument reading and returning one
     * or more GenericData objects created from that reading
     */
    @Test
    public void parseReading() {
        WristInertialHandler inertialHandler = new WristInertialHandler(null, null);
        long testTimestamp = 5000;
        byte[] testInertialData = {
                0x11, 0x22, 0x33, 0x44, 0x55, 0x66
        };

        List<GenericData> dataValues = inertialHandler.parseReading(testInertialData, testTimestamp);
        Assert.assertEquals(3, dataValues.size());

        Assert.assertEquals(0x00001122,dataValues.get(0).getValue());
        Assert.assertEquals(0x00003344,dataValues.get(1).getValue());
        Assert.assertEquals(0x00005566,dataValues.get(2).getValue());

        Assert.assertEquals(testTimestamp, dataValues.get(0).getTimestamp());
        Assert.assertEquals(testTimestamp, dataValues.get(1).getTimestamp());
        Assert.assertEquals(testTimestamp, dataValues.get(2).getTimestamp());

        Assert.assertEquals(DataType.WRIST_INERTIA_X, dataValues.get(0).getType());
        Assert.assertEquals(DataType.WRIST_INERTIA_Y, dataValues.get(1).getType());
        Assert.assertEquals(DataType.WRIST_INERTIA_Z, dataValues.get(2).getType());
    }
}