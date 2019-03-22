package edu.ncsu.csc.assist.data.device;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;

import edu.ncsu.csc.assist.data.cloud.DataStorer;
import edu.ncsu.csc.assist.data.cloud.DataUploader;

public class DataReceiver{

    private static DataStorer storer;
    private static DataUploader uploader;
    private static DataDistributor distributor;
    private static boolean initialized = false;

    public static void initialize(Context context, GoogleApiClient apiClient) {
        storer = new DataStorer(context);
        uploader = new DataUploader(context, apiClient);
        distributor = new DataDistributor(storer);
        storer.startSaveTask();
        uploader.startUploadTask();
        initialized = true;
    }

    public static void receiveChestStreamOne(byte[] data){
        if(!initialized){
            System.out.println("DataReceiver not initialized");
            return;
        }
        distributor.distributeChestStreamOne(data, getTime());
    }
    public static void receiveChestStreamTwo(byte[] data){
        if(!initialized){
            System.out.println("DataReceiver not initialized");
            return;
        }
    distributor.distributeChestStreamTwo(data, getTime());
    }
    public static void receiveWristStreamOne(byte[] data){
        if(!initialized){
            System.out.println("DataReceiver not initialized");
            return;
        }
        distributor.distributeWristStreamOne(data, getTime());
    }
    public static void receiveWristStreamTwo(byte[] data){
        if(!initialized){
            System.out.println("DataReceiver not initialized");
            return;
        }
        distributor.distributeWristStreamTwo(data, getTime());
    }

    private static long getTime(){
        return System.currentTimeMillis();
    }
    public static boolean isInitialized(){
        return initialized;
    }

}
