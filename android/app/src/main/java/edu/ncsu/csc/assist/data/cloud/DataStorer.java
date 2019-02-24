package edu.ncsu.csc.assist.data.cloud;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import androidx.room.Room;
import edu.ncsu.csc.assist.data.objects.GenericData;
import edu.ncsu.csc.assist.data.sqlite.AppDatabase;
import edu.ncsu.csc.assist.data.sqlite.entities.RawDataPoint;

public class DataStorer {

    // Queue of data waiting to be saved to the database
    private Queue<GenericData> saveQueue;

    // Database
    private AppDatabase database;

    // Scheduler Service
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> saveTask;

    public DataStorer(Context context) {
        saveQueue = new LinkedBlockingQueue<>(250);
        database = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "ASSIST").build();
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

    /**
     * Add data to a queue to be saved to the database.
     * All data passed through this method will be guaranteed to be saved under normal operation.
     *
     * @param data
     */
    public void save(GenericData data) {
        saveQueue.add(data);
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
            if(saveQueue.isEmpty()){
                return;
            }
            database.beginTransaction();

            List<RawDataPoint> toInsert = new ArrayList<>(saveQueue.size());
            for (GenericData genericData : saveQueue) {
                toInsert.add(new RawDataPoint(genericData.getType(), genericData.getTimestamp(), genericData.getValue()));
            }

            database.rawDataPointDao().insertAll(toInsert);

            database.setTransactionSuccessful();
            database.endTransaction();
        }
    };
}
