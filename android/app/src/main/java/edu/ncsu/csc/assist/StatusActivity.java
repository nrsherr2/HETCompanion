package edu.ncsu.csc.assist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import edu.ncsu.csc.assist.data.device.DataReceiver;
import edu.ncsu.csc.assist.data.device.DataStream;

public class StatusActivity extends AppCompatActivity {

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> statusTask;

    //the following block deals with monitoring stream activity and reactively setting UI elements to represent their status
    private final long ACTIVE_THRESHOLD = 1000; //time since latest update stream must be under to be considered "active" (in millis)
    private boolean chestStreamOneActive = false;
    private boolean chestStreamTwoActive = false;
    private boolean wristStreamOneActive = false;
    private boolean wristStreamTwoActive = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        //creates scheduled task to update stream statuses
        scheduler = Executors.newSingleThreadScheduledExecutor();
        statusTask = scheduler.scheduleAtFixedRate(updateStreamStatus, 0, ACTIVE_THRESHOLD, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (statusTask == null) {
            return;
        }
        statusTask.cancel(false);
    }

    /**
     * periodic task that reactively checks the multiple stream connections
     * "checking" streams is done by remembering the timestamp of the most recent update
     * then checking to see if that time occurred recently*
     * *recently is defined by a specified threshold
     */
    private final Runnable updateStreamStatus = new Runnable() {
        @Override
        public void run() {
            try {
                System.out.println("ran Task");
                long currentTime = System.currentTimeMillis();
                long chestStreamOneDiff = currentTime - DataReceiver.getLatestTimestamp(DataStream.CHEST_ONE);
                long chestStreamTwoDiff = currentTime - DataReceiver.getLatestTimestamp(DataStream.CHEST_TWO);
                long wristStreamOneDiff = currentTime - DataReceiver.getLatestTimestamp(DataStream.WRIST_ONE);
                long wristStreamTwoDiff = currentTime - DataReceiver.getLatestTimestamp(DataStream.WRIST_TWO);

                //determines if streams need to change state
                //Chest One (fff5)
                if (chestStreamOneActive && chestStreamOneDiff > ACTIVE_THRESHOLD) {
                    ImageView status = findViewById(R.id.chestOneStatus);
                    status.setImageResource(R.drawable.red);
                    chestStreamOneActive = false;
                } else if (!chestStreamOneActive && chestStreamOneDiff < ACTIVE_THRESHOLD) {
                    ImageView status = findViewById(R.id.chestOneStatus);
                    status.setImageResource(R.drawable.green);
                    chestStreamOneActive = true;
                }
                //Chest Two (fff2)
                if (chestStreamTwoActive && chestStreamTwoDiff > ACTIVE_THRESHOLD) {
                    ImageView status = findViewById(R.id.chestTwoStatus);
                    status.setImageResource(R.drawable.red);
                    chestStreamTwoActive = false;
                } else if (!chestStreamTwoActive && chestStreamTwoDiff < ACTIVE_THRESHOLD) {
                    ImageView status = findViewById(R.id.chestTwoStatus);
                    status.setImageResource(R.drawable.green);
                    chestStreamTwoActive = true;
                }
                //Wrist One (fff4)
                if (wristStreamOneActive && wristStreamOneDiff > ACTIVE_THRESHOLD) {
                    ImageView status = findViewById(R.id.wristOneStatus);
                    status.setImageResource(R.drawable.red);
                    wristStreamOneActive = false;
                } else if (!wristStreamOneActive && wristStreamOneDiff < ACTIVE_THRESHOLD) {
                    ImageView status = findViewById(R.id.wristOneStatus);
                    status.setImageResource(R.drawable.green);
                    wristStreamOneActive = true;
                }
                //Wrist Two (fff3)
                if (wristStreamTwoActive && wristStreamTwoDiff > ACTIVE_THRESHOLD) {
                    ImageView status = findViewById(R.id.wristTwoStatus);
                    status.setImageResource(R.drawable.red);
                    wristStreamTwoActive = false;
                } else if (!wristStreamTwoActive && wristStreamTwoDiff < ACTIVE_THRESHOLD) {
                    ImageView status = findViewById(R.id.wristTwoStatus);
                    status.setImageResource(R.drawable.green);
                    wristStreamTwoActive = true;
                }
            } catch (Exception e) {
                System.out.println("Error running status task: " + e.getMessage());
            }
        }
    };
}
