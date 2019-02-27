package edu.ncsu.csc.assist.data.handling;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import edu.ncsu.csc.assist.data.objects.DataType;
import edu.ncsu.csc.assist.data.objects.GenericData;

import static org.junit.Assert.*;

public class WristEnvironmentalHandlerTest {

    /**
     * Tests the parseReading function of the handler.
     * This test is responsible for receive bytes of a single instrument reading and returning one
     * or more GenericData objects created from that reading
     */
    @Test
    public void parseReading() {
        WristEnvironmentalHandler environmentalHandler = new  WristEnvironmentalHandler(null);
        long testTimestamp = 5000;
        byte[] testEnvironmentalData = {
                0x11, 0x22, 0x33, 0x44,
        };

        List<GenericData> dataValues = environmentalHandler.parseReading(testEnvironmentalData, testTimestamp);
        Assert.assertEquals(2, dataValues.size());

        Assert.assertEquals(0x00001122,dataValues.get(0).getValue());
        Assert.assertEquals(0x00003344,dataValues.get(1).getValue());

        Assert.assertEquals(testTimestamp, dataValues.get(0).getTimestamp());
        Assert.assertEquals(testTimestamp, dataValues.get(1).getTimestamp());

        Assert.assertEquals(DataType.WRIST_TEMPERATURE, dataValues.get(0).getType());
        Assert.assertEquals(DataType.WRIST_HUMIDITY, dataValues.get(1).getType());
    }
}