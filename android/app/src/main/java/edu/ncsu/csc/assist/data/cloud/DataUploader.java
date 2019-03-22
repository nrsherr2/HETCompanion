package edu.ncsu.csc.assist.data.cloud;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import androidx.room.Room;
import edu.ncsu.csc.assist.data.sqlite.AppDatabase;
import edu.ncsu.csc.assist.data.sqlite.entities.ConfigOption;
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

    private GoogleApiClient googleApiClient;

    // Scheduler Service
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> uploadTask;

    public DataUploader(Context context, GoogleApiClient googleApiClient) {
        this.mContext = context;
        this.googleApiClient = googleApiClient;
        database = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "ASSIST").allowMainThreadQueries().build();
        restQueue = new RestQueue(context);
    }

    public void startUploadTask() {
        Log.d(getClass().getCanonicalName(), "Starting cloud upload task.");
        //uploadTask = scheduler.scheduleAtFixedRate(uploadData, 30, 30, TimeUnit.MINUTES);
        uploadTask = scheduler.scheduleAtFixedRate(uploadData, 30, 30, TimeUnit.SECONDS);
    }

    private void stopUploadTask() {
        Log.d(getClass().getCanonicalName(), "Stopping cloud upload task.");
        if (uploadTask == null)
            return;
        uploadTask.cancel(false);
    }

    private class DataUploadTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            System.out.println("*** Uploading Info ***");
            Log.d(getClass().getCanonicalName(), "Attempting to upload");
            // Check the wifi connection
            ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean connectedToWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;

            // If not connected to wifi then return; only upload via wifi
            if (!connectedToWiFi){
                Log.d(getClass().getCanonicalName(), "Not connected to WiFi.");
                return null;
            }

            final List<RawDataPoint> toUpload = database.rawDataPointDao().getAll();
            if (toUpload.size() <= 0) {
                Log.d(getClass().getCanonicalName(), "No data to upload.");
                return null;
            }

            // Modify the timestamps by the defined delta
            String userId = database.configOptionDao().getByKey("config_user_id");
            if (userId == null || userId.length() <= 0) {
                Log.e(getClass().getCanonicalName(), "User ID Config does not exist in the database! Aborting upload...");
                return null;
            }
            String sDelta = database.configOptionDao().getByKey("user_" + userId + "_ts_delta");
            if (sDelta == null || sDelta.length() <= 0) {
                // User has never uploaded data before. Lets create an offset from the lowest timestamp in the data uploaded.
                long smallestTimestamp = Long.MAX_VALUE;
                for (RawDataPoint raw : toUpload) {
                    if (raw.getTimestamp() < smallestTimestamp)
                        smallestTimestamp = raw.getTimestamp();
                }
                // Remove 1 second off the timestamp so we don't have and 0 timestamps. The server doesn't seem to really like those.
                smallestTimestamp -= 1000L;

                // Save the timestamp for next time
                database.configOptionDao().insert(new ConfigOption("user_" + userId + "_ts_delta", Long.toString(smallestTimestamp)));
                sDelta = Long.toString(smallestTimestamp);
            }
            long delta = Long.valueOf(sDelta);
            for (RawDataPoint data : toUpload) {
                data.setTimestamp(data.getTimestamp() - delta);
            }

            String hetVersion = database.configOptionDao().getByKey("config_het_version");
            if (hetVersion == null) {
                Log.e(getClass().getCanonicalName(), "HET Version does not exist in the database! Aborting upload...");
                return null;
            }

            Log.d(getClass().getCanonicalName(), "Uploading " + toUpload.size() + " data points to the cloud.");
            try {
                OptionalPendingResult<GoogleSignInResult> pendingResult = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
                String googleIdToken = pendingResult.await().getSignInAccount().getIdToken();

                restQueue.sendSaveRequest(userId, hetVersion, googleIdToken, toUpload, new Response.Listener<JSONObject>() {
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
            return null;
        }
    }

    private final Runnable uploadData = new Runnable() {
        public synchronized void run() {
            new DataUploadTask().execute();
        }
    };

    public void flush() {
        Log.d(getClass().getCanonicalName(), "Flushing uploader...");
        uploadData.run();
    }
}
