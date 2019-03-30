package edu.ncsu.csc.assist.data.sqlite.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import edu.ncsu.csc.assist.data.objects.ProcessedDataType;
import edu.ncsu.csc.assist.data.sqlite.AppDatabase;
import edu.ncsu.csc.assist.data.sqlite.access.ProcessedDataPointDao;
import edu.ncsu.csc.assist.data.sqlite.entities.ProcessedDataPoint;

public class ProcessedDataRepository {

    private ProcessedDataPointDao dao;
    private LiveData<ProcessedDataPoint> heartRate;
    private LiveData<ProcessedDataPoint> hrv;
    private LiveData<ProcessedDataPoint> ozone;

    public ProcessedDataRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        dao = db.processedDataPointDao();
        heartRate = dao.getMostRecent(ProcessedDataType.HEARTRATE);
        hrv = dao.getMostRecent(ProcessedDataType.HRV);
        ozone = dao.getMostRecent(ProcessedDataType.WRIST_OZ);
    }

    public LiveData<ProcessedDataPoint> getHeartRate() {
        return heartRate;
    }

    public LiveData<ProcessedDataPoint> getHRV() {
        return hrv;
    }

    public LiveData<ProcessedDataPoint> getOzone() {
        return ozone;
    }

    public void insert(ProcessedDataPoint dataPoint) {
        new insertAsyncTask(dao).execute(dataPoint);
    }

    private static class insertAsyncTask extends AsyncTask<ProcessedDataPoint, Void, Void> {

        private ProcessedDataPointDao dao;

        insertAsyncTask(ProcessedDataPointDao dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(final ProcessedDataPoint... params) {
            for (ProcessedDataPoint p : params)
                dao.insert(p);
            return null;
        }
    }
}
