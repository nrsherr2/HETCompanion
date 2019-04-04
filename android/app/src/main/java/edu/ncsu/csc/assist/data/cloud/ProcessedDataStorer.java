package edu.ncsu.csc.assist.data.cloud;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import edu.ncsu.csc.assist.data.objects.ProcessedData;
import edu.ncsu.csc.assist.data.sqlite.AppDatabase;
import edu.ncsu.csc.assist.data.sqlite.entities.ProcessedDataPoint;

/**
 * This class is tasked with saving data to the local SQLite database to upload to the cloud later
 * Requests to the database are cached in a queue and sent in batches
 */
public class ProcessedDataStorer {

    // Queue of data waiting to be saved to the database
    private Queue<ProcessedData> saveQueue;

    // Database
    private AppDatabase database;

    // Scheduler Service
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> saveTask;

    public ProcessedDataStorer(Context context) {
        saveQueue = new LinkedBlockingQueue<>();
        database = AppDatabase.getDatabase(context.getApplicationContext());
    }

    public ProcessedDataStorer withDatabase(AppDatabase database) {
        this.database = database;
        return this;
    }

    public ProcessedDataStorer startSaveTask() {
        saveTask = scheduler.scheduleAtFixedRate(dumpQueueToDatabase, 1, 1, TimeUnit.SECONDS);
        return this;
    }

    public void stopSaveTask() {
        if (saveTask == null)
            return;
        saveTask.cancel(false);
        while (!saveQueue.isEmpty()) {
            Log.d(getClass().getCanonicalName(), "Save queue is not empty after stop request, dumping to database...");
            dumpQueueToDatabase.run();
        }
    }

    /**
     * Add data to a queue to be saved to the database.
     * All data passed through this method will be guaranteed to be saved under normal operation.
     *
     * @param data
     */
    public void save(Collection<ProcessedData> data) {
        saveQueue.addAll(data);
    }

    /**
     * Add data to a queue to be saved to the database.
     * All data passed through this method will be guaranteed to be saved under normal operation.
     *
     * @param data
     */
    public void save(ProcessedData data) {
        saveQueue.add(data);
    }

    private final Runnable dumpQueueToDatabase = new Runnable() {
        public synchronized void run() {
            Log.d(getClass().getCanonicalName(), "Dumping the processed data queue to database");
            if (saveQueue.isEmpty()) {
                return;
            }
            database.beginTransaction();

            List<ProcessedDataPoint> toInsert = new ArrayList<>(saveQueue.size());
            for (ProcessedData processedData : saveQueue) {
                toInsert.add(new ProcessedDataPoint(processedData.getType(), processedData.getTimestamp(), processedData.getValue()));
            }

            Log.d(getClass().getCanonicalName(), "Inserting " + toInsert.size() + " processed data points into sqlite database.");
            database.processedDataPointDao().insertAll(toInsert);

            database.setTransactionSuccessful();
            database.endTransaction();

            saveQueue.clear();
        }
    };

    public void flush() {
        dumpQueueToDatabase.run();
    }
}
