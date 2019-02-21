package edu.ncsu.csc.assist.data.cloud;

import android.content.Context;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import androidx.room.Room;
import edu.ncsu.csc.assist.data.sqlite.AppDatabase;

public class DataUploader {

    // Database
    private AppDatabase database;

    // Scheduler Service
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> uploadTask;

    public DataUploader(Context context) {
        database = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "ASSIST").build();
        startUploadTask();
    }

    private void startUploadTask() {
        uploadTask = scheduler.scheduleAtFixedRate(uploadData, 1, 1, TimeUnit.MINUTES);
    }

    private void stopUploadTask() {
        if (uploadTask == null)
            return;
        uploadTask.cancel(false);
    }

    private final Runnable uploadData = new Runnable() {
        public void run() {
            //TODO Check if database has new data
            database.beginTransaction();

            //Poll/upload data
            //remove uploaded data from the database

            database.setTransactionSuccessful();
            database.endTransaction();
        }
    };
}
