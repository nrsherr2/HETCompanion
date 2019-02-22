package edu.ncsu.csc.assist.data.cloud;

import android.content.Context;
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

public class DataUploader {

    // Database
    private AppDatabase database;

    // RestQueue
    private RestQueue restQueue;

    // Scheduler Service
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> uploadTask;

    public DataUploader(Context context) {
        database = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "ASSIST").build();
        restQueue = new RestQueue(context);
        startUploadTask();
    }

    private void startUploadTask() {
        Log.d(getClass().getCanonicalName(), "Starting cloud upload task.");
        uploadTask = scheduler.scheduleAtFixedRate(uploadData, 1, 1, TimeUnit.MINUTES);
    }

    private void stopUploadTask() {
        Log.d(getClass().getCanonicalName(), "Stopping cloud upload task.");
        if (uploadTask == null)
            return;
        uploadTask.cancel(false);
    }

    private final Runnable uploadData = new Runnable() {
        public void run() {
            final List<RawDataPoint> toUpload = database.rawDataPointDao().getAll();
            if (toUpload.size() <= 0) {
                return;
            }

            Log.d(getClass().getCanonicalName(), "Uploading " + toUpload.size() + " data points to the cloud.");
            try {
                restQueue.makeRequest(toUpload, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                database.beginTransaction();

                                for (RawDataPoint dataPoint : toUpload)
                                    database.rawDataPointDao().deleteById(dataPoint.getId());

                                database.setTransactionSuccessful();
                                database.endTransaction();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(getClass().getCanonicalName(), error.getMessage(), error);
                            }
                        });
            } catch (JSONException e) {
                Log.e(getClass().getCanonicalName(), "JSON Token error! Unable to upload this data!", e);
            }
        }
    };
}
