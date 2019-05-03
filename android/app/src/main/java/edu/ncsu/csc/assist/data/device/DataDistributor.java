package edu.ncsu.csc.assist.data.device;

import java.util.Arrays;

import edu.ncsu.csc.assist.data.cloud.DataStorer;
import edu.ncsu.csc.assist.data.cloud.ProcessedDataStorer;
import edu.ncsu.csc.assist.data.handling.ChestEcgHandler;
import edu.ncsu.csc.assist.data.handling.ChestInertialHandler;
import edu.ncsu.csc.assist.data.handling.ChestPpgHandler;
import edu.ncsu.csc.assist.data.handling.Handler;
import edu.ncsu.csc.assist.data.handling.WristEnvironmentalHandler;
import edu.ncsu.csc.assist.data.handling.WristInertialHandler;
import edu.ncsu.csc.assist.data.handling.WristOzoneHandler;
import edu.ncsu.csc.assist.data.handling.WristPpgHandler;

/**
 * Data Package Structure
 * <p>
 * HET Chest:
 * Stream_One: |ecg1|ecg1|ecg1|ecg2|ecg2|ecg2|ecg3|ecg3|ecg3|ecg4|ecg4|ecg4|ppg1|ppg1|ppg2|ppg2|ppg3|ppg3|ppg4|ppg4|
 * Stream_Two: | x1 | x1 | y1 | y1 | z1 | z1 | x2 | x2 | y2 | y2 | z2 | z2 |pkg#|
 * <p>
 * HET Wrist:
 * Stream_One: | x1 | x1 | y1 | y1 | z1 | z1 | x2 | x2 | y2 | y2 | z2 | z2 |ppg1|ppg1|ppg2|ppg2|pkg#|
 * Stream_Two: |oz1 |oz1 |poz1|poz1|roz1|roz1|moz1|moz1|pkg#|    |    |    |    |    |    |    |tmp1|tmp1|humid1|humid1|
 *
 * DataDistributor is responsible for creating all handlers and correctly feeding the the correct information
 * A change the the device package structure would mean that DataDistributor would also need to be changed
 */
public class DataDistributor {

    //Chest Stream 1
    private Handler chestEcgHandler;
    private Handler chestPpgHandler;

    //Chest Stream 2
    private Handler chestInertialHandler;

    //Wrist Stream 1
    private Handler wristInertialHandler;
    private Handler wristPpgHandler;

    //Wrist Stream 2
    private Handler wristOzoneHandler;
    private Handler wristEnvironmentalHandler;

    //once  handlers are created, the below values will overwritten through the appropriate constructor
    private int CHEST_ECG_BYTES = 12;
    private int CHEST_PPG_BYTES = 8;
    private int CHEST_STREAM_ONE = CHEST_ECG_BYTES + CHEST_PPG_BYTES;

    private int CHEST_INERTIAL_BYTES = 12;
    private int CHEST_STREAM_TWO = CHEST_INERTIAL_BYTES;

    private int WRIST_INERTIAL_BYTES = 12;
    private int WRIST_PPG_BYTES = 4;
    private int WRIST_STREAM_ONE = WRIST_INERTIAL_BYTES + WRIST_PPG_BYTES;

    private int WRIST_OZONE_BYTES = 8;
    private int WRIST_ENVIRONMENTAL_BYTES = 4;
    private int OZONE_ENVIRON_BUFFER_BYTES = 8;
    private int WRIST_STREAM_TWO = WRIST_OZONE_BYTES + OZONE_ENVIRON_BUFFER_BYTES + WRIST_ENVIRONMENTAL_BYTES;

    public DataDistributor(DataStorer rawDataBuffer, ProcessedDataStorer processedDataBuffer) {
        chestEcgHandler = new ChestEcgHandler(rawDataBuffer, processedDataBuffer);
        chestInertialHandler = new ChestInertialHandler(rawDataBuffer, processedDataBuffer);
        chestPpgHandler = new ChestPpgHandler(rawDataBuffer, processedDataBuffer);
        wristEnvironmentalHandler = new WristEnvironmentalHandler(rawDataBuffer, processedDataBuffer);
        wristInertialHandler = new WristInertialHandler(rawDataBuffer, processedDataBuffer);
        wristOzoneHandler = new WristOzoneHandler(rawDataBuffer, processedDataBuffer);
        wristPpgHandler = new WristPpgHandler(rawDataBuffer, processedDataBuffer);

        CHEST_ECG_BYTES = chestEcgHandler.getTotalByteSize();
        CHEST_PPG_BYTES = chestPpgHandler.getTotalByteSize();
        CHEST_INERTIAL_BYTES = chestInertialHandler.getTotalByteSize();
        WRIST_INERTIAL_BYTES = wristInertialHandler.getTotalByteSize();
        WRIST_PPG_BYTES = wristPpgHandler.getTotalByteSize();
        WRIST_OZONE_BYTES = wristOzoneHandler.getTotalByteSize();
        OZONE_ENVIRON_BUFFER_BYTES = 8;
        WRIST_ENVIRONMENTAL_BYTES = wristEnvironmentalHandler.getTotalByteSize();
    }


    public void distributeChestStreamOne(byte[] data, long timestamp) {
        if (data.length < CHEST_STREAM_ONE) {
            throw new IllegalArgumentException("HET Chest data stream one received did not match expected length");
        }
        int offset = 0;

        //--CHEST STREAM ONE DATA PARSING--
        //ECG data
        byte[] ecgData = Arrays.copyOfRange(data, offset, offset + CHEST_ECG_BYTES);
        offset += CHEST_ECG_BYTES;
        chestEcgHandler.handle(ecgData, timestamp);

        //PPG data
        byte[] ppgData = Arrays.copyOfRange(data, offset, offset + CHEST_PPG_BYTES);
        offset += CHEST_PPG_BYTES;
        chestPpgHandler.handle(ppgData, timestamp);
    }

    public void distributeChestStreamTwo(byte[] data, long timestamp) {
        if (data.length < CHEST_STREAM_TWO) {
            throw new IllegalArgumentException("HET Chest data stream two received did not match expected length");
        }

        int offset = 0;
        //--CHEST STREAM TWO DATA PARSING--
        //Inertial data
        byte[] inertialData = Arrays.copyOfRange(data, offset, offset + CHEST_INERTIAL_BYTES);
        offset += CHEST_INERTIAL_BYTES;
        chestInertialHandler.handle(inertialData,timestamp);
    }


    public void distributeWristStreamOne(byte[] data, long timestamp) {
        if (data.length < WRIST_STREAM_ONE) {
            throw new IllegalArgumentException("HET Wrist data stream one received did not match expected length");
        }

        int offset = 0;
        //--WRIST STREAM ONE DATA PARSING--
        //Inertial data
        byte[] inertialData = Arrays.copyOfRange(data, offset, offset + WRIST_INERTIAL_BYTES);
        offset += WRIST_INERTIAL_BYTES;
        wristInertialHandler.handle(inertialData,timestamp);

        //PPG data
        byte[] ppgData = Arrays.copyOfRange(data, offset, offset + WRIST_PPG_BYTES);
        offset += WRIST_PPG_BYTES;
        wristPpgHandler.handle(ppgData,timestamp);
    }

    public void distributeWristStreamTwo(byte[] data, long timestamp) {
        if (data.length < WRIST_STREAM_TWO) {
            throw new IllegalArgumentException("HET Wrist data stream two received did not match expected length");
        }

        int offset = 0;
        //--WRIST STREAM TWO DATA PARSING--
        //Ozone data
        byte[] ozoneData = Arrays.copyOfRange(data, offset, offset + WRIST_OZONE_BYTES);
        offset += WRIST_OZONE_BYTES;
        wristOzoneHandler.handle(ozoneData,timestamp);

        //Extra space before environmental data
        offset += OZONE_ENVIRON_BUFFER_BYTES;

        //Environmental data
        byte[] environmentalData = Arrays.copyOfRange(data, offset, offset + WRIST_ENVIRONMENTAL_BYTES);
        offset += WRIST_ENVIRONMENTAL_BYTES;
        wristEnvironmentalHandler.handle(environmentalData, timestamp);
    }
}
