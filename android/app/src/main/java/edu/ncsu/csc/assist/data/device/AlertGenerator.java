package edu.ncsu.csc.assist.data.device;

import android.content.Context;
import android.widget.Toast;

import edu.ncsu.csc.assist.data.sqlite.AppDatabase;
import edu.ncsu.csc.assist.data.sqlite.access.AlertDao;
import edu.ncsu.csc.assist.data.sqlite.entities.Alert;

public class AlertGenerator {

    private static AlertDao dao;

    public static void initalize(Context context) {
        dao = AppDatabase.getDatabase(context).alertDao();
    }

    public static void createAlert(Context context, Alert.AlertType type, String message) {
        if (dao == null) {
            throw new IllegalStateException("AlertGenerator has not been initialized!");
        }

        dao.insert(new Alert(type.toString(), System.currentTimeMillis(), message, false));

        //TODO replace this with a notification
        Toast.makeText(context, "ALERT: " + type.toString() + " - " + message, Toast.LENGTH_LONG).show();
    }
}
