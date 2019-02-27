package edu.ncsu.csc.assist.data.handling;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import edu.ncsu.csc.assist.data.objects.DataType;
import edu.ncsu.csc.assist.data.objects.GenericData;

import static org.junit.Assert.*;

public class WristOzoneHandlerTest {

    /**
     * Tests the parseReading function of the handler.
     * This test is responsible for receive bytes of a single instrument reading and returning one
     * or more GenericData objects created from that reading
     */
    @Test
    public void parseReading() {
        WristOzoneHandler ozoneHandler = new WristOzoneHandler(null);
        long testTimestamp = 5000;
        byte[] testOzoneData = {
                0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77
        };

        List<GenericData> dataValues = ozoneHandler.parseReading(testOzoneData, testTimestamp);
        Assert.assertEquals(4, dataValues.size());

        Assert.assertEquals(0x00000011,dataValues.get(0).getValue());
        Assert.assertEquals(0x00002233,dataValues.get(1).getValue());
        Assert.assertEquals(0x00004455,dataValues.get(2).getValue());
        Assert.assertEquals(0x00006677,dataValues.get(3).getValue());

        Assert.assertEquals(testTimestamp, dataValues.get(0).getTimestamp());
        Assert.assertEquals(testTimestamp, dataValues.get(1).getTimestamp());
        Assert.assertEquals(testTimestamp, dataValues.get(2).getTimestamp());
        Assert.assertEquals(testTimestamp, dataValues.get(2).getTimestamp());

        Assert.assertEquals(DataType.WRIST_OZ, dataValues.get(0).getType());
        Assert.assertEquals(DataType.WRIST_POZ, dataValues.get(1).getType());
        Assert.assertEquals(DataType.WRIST_ROZ, dataValues.get(2).getType());
        Assert.assertEquals(DataType.WRIST_MOZ, dataValues.get(3).getType());
    }
}