package edu.ncsu.csc.assist.data.device;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;

import edu.ncsu.csc.assist.data.cloud.DataStorer;
import edu.ncsu.csc.assist.data.cloud.DataUploader;
import edu.ncsu.csc.assist.data.cloud.ProcessedDataStorer;
import edu.ncsu.csc.assist.data.sqlite.entities.Alert;

/**
 * DataReceiver is an intermediate class that proceeds DataDistributor in the data flow
 * Through this class, data is statically passed through the DataReceiver and to the DataDistributor
 * Before the class is used, however, DataReceiver must be initialized with Context and GoogleApiClient
 */
public class DataReceiver{

    private static DataStorer rawDataStorer;
    private static ProcessedDataStorer processedDataStorer;
    private static DataUploader uploader;
    private static DataDistributor distributor;
    private static boolean initialized = false;

    //private static int lastPacketChestOne;
    private static int lastPacketChestTwo;
    private static int lastPacketWristOne;
    private static int lastPacketWristTwo;

    private final static int LOST_PACKET_ALERT_THRESHOLD = 50;

    //private int CHEST_ONE_PACKET_INDEX = ?; //Chest_stream_one does not have a packet count
    private static int CHEST_TWO_PACKET_INDEX = 12;
    private static int WRIST_ONE_PACKET_INDEX = 16;
    private static int WRIST_TWO_PACKET_INDEX = 8;

    private static long latestChestStreamOneTime = 0;
    private static long latestChestStreamTwoTime = 0;
    private static long latestWristStreamOneTime = 0;
    private static long latestWristStreamTwoTime = 0;

    public static void initialize(Context context, GoogleApiClient apiClient) {
        rawDataStorer = new DataStorer(context);
        processedDataStorer = new ProcessedDataStorer(context);
        uploader = new DataUploader(context, apiClient);
        distributor = new DataDistributor(rawDataStorer, processedDataStorer);
        rawDataStorer.startSaveTask();
        processedDataStorer.startSaveTask();
        uploader.startUploadTask();
        initialized = true;

        //lastPacketChestOne = -1;
        lastPacketChestTwo = -1;
        lastPacketWristOne = -1;
        lastPacketWristTwo = -1;
    }

    public static void receiveChestStreamOne(byte[] data){
        if(!initialized){
            System.out.println("DataReceiver not initialized");
            return;
        }
        latestChestStreamOneTime = getTime();
        distributor.distributeChestStreamOne(data, getTime());
    }

    public static void receiveChestStreamTwo(byte[] data){
        if(!initialized){
            System.out.println("DataReceiver not initialized");
            return;
        }
        int packetDiff = data[CHEST_TWO_PACKET_INDEX] - lastPacketChestTwo;
        if (packetDiff >= LOST_PACKET_ALERT_THRESHOLD && lastPacketChestTwo != -1) {
            AlertGenerator.createAlert(Alert.AlertType.PACKET_LOSS, "Lost packets for chest inertial data");
        }
        lastPacketChestTwo = data[CHEST_TWO_PACKET_INDEX];
        latestChestStreamTwoTime = getTime();
        distributor.distributeChestStreamTwo(data, getTime());
    }

    //static int heartTime = 0; debug
    public static void receiveWristStreamOne(byte[] data){
        if(!initialized){
            System.out.println("DataReceiver not initialized");
            return;
        }
        int packetDiff = data[WRIST_ONE_PACKET_INDEX] - lastPacketWristOne;
        if (packetDiff >= LOST_PACKET_ALERT_THRESHOLD && lastPacketWristOne != -1) {
            AlertGenerator.createAlert(Alert.AlertType.PACKET_LOSS, "Lost packets for wrist inertial data");
        }
        lastPacketWristOne = data[WRIST_ONE_PACKET_INDEX];
        latestWristStreamOneTime = getTime();
        distributor.distributeWristStreamOne(data, getTime());

        //The following code sends a sign wave to Chest Stream One handlers
        /*
        long time = getTime();
        byte[] chestData = new byte[20];
        for(int i = 0; i < 4; i++){
            chestData[i*3 + 0] = (byte)(((0x00FF0000) & (int)(100*Math.sin(1.0*(heartTime)/100)+100)) >> 16);
            chestData[i*3 + 1] = (byte)(((0x0000FF00) & (int)(100*Math.sin(1.0*(heartTime)/100)+100)) >> 8);
            chestData[i*3 + 2] = (byte)((0x000000FF) & (int)(100*Math.sin(1.0*(heartTime)/100)+100));
            heartTime += 5;
        }
        distributor.distributeChestStreamOne(chestData, time);
        */
    }

    public static void receiveWristStreamTwo(byte[] data){
        if(!initialized){
            System.out.println("DataReceiver not initialized");
            return;
        }
        int packetDiff = data[WRIST_TWO_PACKET_INDEX] - lastPacketWristTwo;
        if (packetDiff >= LOST_PACKET_ALERT_THRESHOLD && lastPacketWristTwo != -1) {
            AlertGenerator.createAlert(Alert.AlertType.PACKET_LOSS, "Lost packets for wrist environmental data");
        }
        lastPacketWristTwo = data[WRIST_TWO_PACKET_INDEX];
        latestWristStreamTwoTime = getTime();
        distributor.distributeWristStreamTwo(data, getTime());
    }

    private static long getTime(){
        return System.currentTimeMillis();
    }
    public static boolean isInitialized(){
        return initialized;
    }

    public static long getLatestTimestamp(DataStream stream) {
        switch (stream) {
            case CHEST_ONE:
                return latestChestStreamOneTime;
            case CHEST_TWO:
                return latestChestStreamTwoTime;
            case WRIST_ONE:
                return latestWristStreamOneTime;
            case WRIST_TWO:
                return latestWristStreamTwoTime;
            default:
                return 0;
        }
    }
}
