package edu.ncsu.csc.assist.data.cloud;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import androidx.room.Room;
import edu.ncsu.csc.assist.data.sqlite.AppDatabase;
import edu.ncsu.csc.assist.data.sqlite.entities.RawDataPoint;

/**
 * This class is tasked with pulling data from the SQLite database and sending a REST request
 * to the cloud. On a successful response, the data is deleted from the local database.
 */
public class DataUploader {

    // Database
    private AppDatabase database;

    // RestQueue
    private RestQueue restQueue;

    private Context mContext;

    // Scheduler Service
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> uploadTask;

    public DataUploader(Context context) {
        this.mContext = context;
        database = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "ASSIST").build();
        restQueue = new RestQueue(context);
        startUploadTask();
    }

    private void startUploadTask() {
        Log.d(getClass().getCanonicalName(), "Starting cloud upload task.");
        uploadTask = scheduler.scheduleAtFixedRate(uploadData, 30, 30, TimeUnit.MINUTES);
    }

    private void stopUploadTask() {
        Log.d(getClass().getCanonicalName(), "Stopping cloud upload task.");
        if (uploadTask == null)
            return;
        uploadTask.cancel(false);
    }

    private final Runnable uploadData = new Runnable() {
        public synchronized void run() {
            Log.d(getClass().getCanonicalName(), "Attempting to upload");
            // Check the wifi connection
            ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean connectedToWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;

            // If not connected to wifi then return; only upload via wifi
            if (!connectedToWiFi) return;

            final List<RawDataPoint> toUpload = database.rawDataPointDao().getAll();
            if (toUpload.size() <= 0) {
                return;
            }

            // Modify the timestamps by the defined delta
            String userId = database.configOptionDao().getByKey("config_user_id");
            long delta = Long.valueOf(database.configOptionDao().getByKey("user_" + userId + "_ts_delta"));
            for (RawDataPoint data : toUpload) {
                data.setTimestamp(data.getTimestamp() - delta);
            }

            Log.d(getClass().getCanonicalName(), "Uploading " + toUpload.size() + " data points to the cloud.");
            try {
                restQueue.sendSaveRequest(userId, database.configOptionDao().getByKey("config_het_version"), toUpload, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    if (response.getInt("status") == 201) {
                                        database.beginTransaction();

                                        for (RawDataPoint dataPoint : toUpload)
                                            database.rawDataPointDao().deleteById(dataPoint.getId());

                                        database.setTransactionSuccessful();
                                        database.endTransaction();
                                    } else {
                                        throw new IllegalStateException("REST API Returned " + response.getInt("status") + "! Error: " + response.getString("message"));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // Fail, do not delete data from the database
                                Log.e(getClass().getCanonicalName(), error.getMessage(), error);
                            }
                        });
            } catch (JSONException e) {
                Log.e(getClass().getCanonicalName(), "JSON Token error! Unable to upload this data!", e);
            }
        }
    };

    public void flush(){
        Log.d(getClass().getCanonicalName(), "Flushing uploader...");
        uploadData.run();
    }
}
