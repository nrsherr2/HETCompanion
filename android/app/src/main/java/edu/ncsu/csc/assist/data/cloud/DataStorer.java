package edu.ncsu.csc.assist.data.cloud;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import edu.ncsu.csc.assist.data.objects.GenericData;
import edu.ncsu.csc.assist.data.sqlite.DatabaseContract;
import edu.ncsu.csc.assist.data.sqlite.SQLiteManager;

public class DataStorer {

    // Queue of data waiting to be saved to the database
    private Queue<GenericData> saveQueue;

    // Database
    private SQLiteDatabase database;

    // Scheduler Service
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> saveTask;

    public DataStorer(Context context) {
        saveQueue = new LinkedBlockingQueue<>(250);
        database = new SQLiteManager(context).getWritableDatabase();
        startSaveTask();
    }

    /**
     * Add data to a queue to be saved to the database.
     * All data passed through this method will be guaranteed to be saved under normal operation.
     *
     * @param data
     */
    public void save(Collection<GenericData> data) {
        saveQueue.addAll(data);
    }

    private void startSaveTask() {
        saveTask = scheduler.scheduleAtFixedRate(dumpQueueToDatabase, 1, 1, TimeUnit.SECONDS);
    }

    private void stopSaveTask() {
        if (saveTask == null)
            return;
        saveTask.cancel(false);
        while (!saveQueue.isEmpty()) {
            dumpQueueToDatabase.run();
        }
    }

    private final Runnable dumpQueueToDatabase = new Runnable() {
        public void run() {
            database.beginTransaction();

            for (GenericData data : saveQueue) {
                ContentValues values = new ContentValues();
                values.put(DatabaseContract.RawData.COLUMN_NAME_TYPE, data.getType().getId());
                values.put(DatabaseContract.RawData.COLUMN_NAME_TIMESTAMP, data.getTimestamp());
                values.put(DatabaseContract.RawData.COLUMN_NAME_VALUE, data.getValue());
                database.insert(DatabaseContract.RawData.TABLE_NAME, null, values);
            }

            database.setTransactionSuccessful();
            database.endTransaction();
        }
    };
}
