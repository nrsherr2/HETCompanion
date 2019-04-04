package edu.ncsu.csc.assist.data.sqlite.repository;

import android.app.Application;
import android.os.AsyncTask;

import edu.ncsu.csc.assist.data.sqlite.AppDatabase;
import edu.ncsu.csc.assist.data.sqlite.access.RawDataPointDao;
import edu.ncsu.csc.assist.data.sqlite.entities.RawDataPoint;

/*
 * This class is currently not being used,
 */
public class RawDataRepository {

    private RawDataPointDao dao;

    public RawDataRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        dao = db.rawDataPointDao();
    }

    public void insert(RawDataPoint dataPoint) {
        new insertAsyncTask(dao).execute(dataPoint);
    }

    private static class insertAsyncTask extends AsyncTask<RawDataPoint, Void, Void> {

        private RawDataPointDao dao;

        insertAsyncTask(RawDataPointDao dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(final RawDataPoint... params) {
            for (RawDataPoint p : params)
                dao.insert(p);
            return null;
        }
    }
}
