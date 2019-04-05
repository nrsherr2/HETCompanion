package edu.ncsu.csc.assist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class EnvFragment extends DetailFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public LineGraphSeries<DataPoint> getDataPoints(String graphView) {

        LineGraphSeries<DataPoint> series;

        // todo: get datapoints from database based on spinner selection
        switch(graphView.toLowerCase())
        {
            case "live" :
                // Statements
                // todo replace me with db points
                series = new LineGraphSeries<>(arrayToDatapoints(new double[] {0, 1, 2, 3, 4}));
                break; // break is optional

            case "minute" :
                // Statements
                // todo replace me with db points
                series = new LineGraphSeries<>(arrayToDatapoints(new double[] {4, 3, 2, 1, 0}));
                break; // break is optional

            case "hour" :
                // Statements
                // todo replace me with db points
                series = new LineGraphSeries<>(arrayToDatapoints(new double[] {4, 3, 2, 1, 0}));
                break; // break is optional

            case "day" :
                // Statements
                // todo replace me with db points
                series = new LineGraphSeries<>(arrayToDatapoints(new double[] {4, 3, 2, 1, 0}));
                break; // break is optional

            case "week" :
                // Statements
                // todo replace me with db points
                series = new LineGraphSeries<>(arrayToDatapoints(new double[] {4, 3, 2, 1, 0}));
                break; // break is optional

            case "month" :
                // Statements
                // todo replace me with db points
                series = new LineGraphSeries<>(arrayToDatapoints(new double[] {4, 3, 2, 1, 0}));
                break; // break is optional

            default :
                // Statements
                series = new LineGraphSeries<>(arrayToDatapoints(new double[] {1, 5, 3, 2, 6}));
        }

        return series;
    }

    @Override
    public String getTitle() {
        return getString(R.string.env);
    }
}
