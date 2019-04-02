package edu.ncsu.csc.assist.data.device;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;

import edu.ncsu.csc.assist.data.cloud.DataStorer;
import edu.ncsu.csc.assist.data.cloud.DataUploader;
import edu.ncsu.csc.assist.data.cloud.ProcessedDataStorer;

public class DataReceiver{

    private static DataStorer rawDataStorer;
    private static ProcessedDataStorer processedDataStorer;
    private static DataUploader uploader;
    private static DataDistributor distributor;
    private static boolean initialized = false;

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
        latestChestStreamTwoTime = getTime();
        distributor.distributeChestStreamTwo(data, getTime());
    }
    public static void receiveWristStreamOne(byte[] data){
        if(!initialized){
            System.out.println("DataReceiver not initialized");
            return;
        }
        latestWristStreamOneTime = getTime();
        distributor.distributeWristStreamOne(data, getTime());
    }
    public static void receiveWristStreamTwo(byte[] data){
        if(!initialized){
            System.out.println("DataReceiver not initialized");
            return;
        }
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
