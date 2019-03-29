package edu.ncsu.csc.assist;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ThreadLocalRandom;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import edu.ncsu.csc.assist.data.sqlite.entities.ProcessedDataPoint;
import edu.ncsu.csc.assist.data.sqlite.repository.ProcessedDataRepository;

public class HomeFragment extends DetailFragment {

    private TextView hrLive, hrvLive, ozoneLive;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        hrLive = view.findViewById(R.id.heartRateLive);
        hrvLive = view.findViewById(R.id.heartRateVariabilityLive);
        ozoneLive = view.findViewById(R.id.ozoneLive);

        final ProcessedDataRepository processedDataRepository = new ProcessedDataRepository(getActivity().getApplication());

        processedDataRepository.getHeartRate().observe(this, new Observer<ProcessedDataPoint>() {
            @Override
            public void onChanged(@Nullable final ProcessedDataPoint dataPoint) {
                Log.d(getClass().getCanonicalName(), "Updating BPM on dashboard");
                if (dataPoint != null) {
                    hrLive.setText(Integer.toString(dataPoint.getValue()));
                } else {
                    hrLive.setText("0");
                }
            }
        });

        processedDataRepository.getHRV().observe(this, new Observer<ProcessedDataPoint>() {
            @Override
            public void onChanged(@Nullable final ProcessedDataPoint dataPoint) {
                Log.d(getClass().getCanonicalName(), "Updating HRV on dashboard");
                if (dataPoint != null) {
                    hrvLive.setText(Integer.toString(dataPoint.getValue()));
                } else {
                    hrvLive.setText("0");
                }
            }
        });

        processedDataRepository.getOzone().observe(this, new Observer<ProcessedDataPoint>() {
            @Override
            public void onChanged(@Nullable final ProcessedDataPoint dataPoint) {
                Log.d(getClass().getCanonicalName(), "Updating Ozone on dashboard");
                if (dataPoint != null) {
                    ozoneLive.setText(Integer.toString(dataPoint.getValue()));
                } else {
                    ozoneLive.setText("0");
                }
            }
        });

        //TODO remove this and button in layout before merging to dev
        Button randomHR = view.findViewById(R.id.randomHR);
        randomHR.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(view.getContext(), "Inserting value", Toast.LENGTH_SHORT).show();
                processedDataRepository.insert(new ProcessedDataPoint("bpm", System.currentTimeMillis(), ThreadLocalRandom.current().nextInt(1, 100)));
            }
        });
    }
}