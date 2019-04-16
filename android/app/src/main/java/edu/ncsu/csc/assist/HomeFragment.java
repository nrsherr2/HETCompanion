package edu.ncsu.csc.assist;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import edu.ncsu.csc.assist.data.sqlite.entities.ProcessedDataPoint;
import edu.ncsu.csc.assist.data.sqlite.repository.ProcessedDataRepository;

public class HomeFragment extends Fragment {

    private TextView hrLive, hrvLive, ozoneLive;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        hrLive = view.findViewById(R.id.hrLive);
        hrvLive = view.findViewById(R.id.hrvLive);
        ozoneLive = view.findViewById(R.id.ozoneLive);

        final ProcessedDataRepository processedDataRepository = new ProcessedDataRepository(getActivity().getApplication());

        processedDataRepository.getHeartRate().observe(this, new Observer<ProcessedDataPoint>() {
            @Override
            public void onChanged(@Nullable final ProcessedDataPoint dataPoint) {
                Log.d(getClass().getCanonicalName(), "Updating BPM on dashboard");
                if (dataPoint != null) {
                    hrLive.setText(String.format("%.2f", dataPoint.getValue()));
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
                    hrvLive.setText(String.format("%.2f", dataPoint.getValue()));
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
                    ozoneLive.setText(String.format("%.2f", dataPoint.getValue()));
                } else {
                    ozoneLive.setText("0");
                }
            }
        });
    }
}
