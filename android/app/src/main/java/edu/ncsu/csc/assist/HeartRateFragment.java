package edu.ncsu.csc.assist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class HeartRateFragment extends DetailFragment  {

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
        switch(graphView)
        {
            case "live" :
                // Statements
                // todo replace me with db points
                series = new LineGraphSeries<>(arrayToDatapoints(new int[] {0, 1, 2, 3, 4}));
                break; // break is optional

            case "minute" :
                // Statements
                // todo replace me with db points
                series = new LineGraphSeries<>(arrayToDatapoints(new int[] {4, 3, 2, 1, 0}));
                break; // break is optional

            default :
                // Statements
                series = new LineGraphSeries<>(arrayToDatapoints(new int[] {1, 5, 3, 2, 6}));
        }

        return series;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // get the selection
        String graphView = parent.getItemAtPosition(position).toString();
        // get the points for that selection
        LineGraphSeries<DataPoint> series = getDataPoints(graphView);
        //update the graph
        setGraphData(series);
    }

    // do nothing
    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
}
