package edu.ncsu.csc.assist.data.handling;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import edu.ncsu.csc.assist.data.cloud.DataStorer;
import edu.ncsu.csc.assist.data.handling.Handler;
import edu.ncsu.csc.assist.data.objects.GenericData;

import static org.junit.Assert.*;

/**
 * Tests the functionality of the Handler super-class
 */
public class HandlerTest {

    /**
     * Tests static method getIntFromByte  functionality
     */
    @Test
    public void getIntFromBytes(){
        byte[] bytesEmpty = {};
        Assert.assertEquals(0, Handler.getIntFromBytes(bytesEmpty));

        byte[] bytesTooShort = {0x01, 0x23, 0x45};
        Assert.assertEquals(0x00012345, Handler.getIntFromBytes(bytesTooShort));

        byte[] bytesTooLong = {0x11, 0x22, 0x33, 0x44, 0x55};
        Assert.assertEquals(0x22334455, Handler.getIntFromBytes(bytesTooLong));

        byte[] bytesJustRight = {0x01, 0x23, 0x45, 0x67};
        Assert.assertEquals(0x01234567, Handler.getIntFromBytes(bytesJustRight));
    }

    /**
     * Tests the parseInput functionality
     *
     * parseInput of Handler calls the abstract method parseReading. Therefore, to test the method,
     * a Handler object must be constructed as a certain child handler. To test the validity of Handler.parseInput,
     * a ChestEcgHandler and ChestInertialHandler are used.
     */
    @Test
    public void parseInput(){
        byte[] testEcgData = {
                0x11, 0x22, 0x33,
                0x44, 0x55, 0x66,
                0x11, 0x22, 0x33,
                0x44, 0x55, 0x66
        };
        Handler ecgHandler = new ChestEcgHandler(null);
        List<GenericData> ecgValues = ecgHandler.parseInput(testEcgData, 0);
        Assert.assertEquals(ecgHandler.getNumberOfValues(), ecgValues.size());

        Assert.assertEquals(0x00112233, ecgValues.get(0).getValue());
        Assert.assertEquals(0x00445566, ecgValues.get(1).getValue());
        Assert.assertEquals(0x00112233, ecgValues.get(2).getValue());
        Assert.assertEquals(0x00445566, ecgValues.get(3).getValue());

        Assert.assertEquals(0, ecgValues.get(0).getTimestamp());
        Assert.assertEquals(1 * ecgHandler.timeBetweenValues, ecgValues.get(1).getTimestamp());
        Assert.assertEquals(2 * ecgHandler.timeBetweenValues, ecgValues.get(2).getTimestamp());
        Assert.assertEquals(3 * ecgHandler.timeBetweenValues, ecgValues.get(3).getTimestamp());


        byte[] testInertialData = {
                0x11, 0x22,
                0x33, 0x44,
                0x55, 0x66,
                0x77, 0x11,
                0x22, 0x33,
                0x44, 0x55
        };
        Handler inertialHandler = new ChestInertialHandler(null);
        List<GenericData> inertialValues = inertialHandler.parseInput(testInertialData, 0);
        Assert.assertEquals(inertialHandler.getNumberOfValues()*3, inertialValues.size());

        Assert.assertEquals(0x00001122, inertialValues.get(0).getValue());
        Assert.assertEquals(0x00003344, inertialValues.get(1).getValue());
        Assert.assertEquals(0x00005566, inertialValues.get(2).getValue());
        Assert.assertEquals(0x00007711, inertialValues.get(3).getValue());
        Assert.assertEquals(0x00002233, inertialValues.get(4).getValue());
        Assert.assertEquals(0x00004455, inertialValues.get(5).getValue());

        Assert.assertEquals(0, inertialValues.get(0).getTimestamp());
        Assert.assertEquals(0, inertialValues.get(1).getTimestamp());
        Assert.assertEquals(0, inertialValues.get(2).getTimestamp());
        Assert.assertEquals(1 * inertialHandler.timeBetweenValues, inertialValues.get(3).getTimestamp());
        Assert.assertEquals(1 * inertialHandler.timeBetweenValues, inertialValues.get(4).getTimestamp());
        Assert.assertEquals(1 * inertialHandler.timeBetweenValues, inertialValues.get(5).getTimestamp());
    }
}