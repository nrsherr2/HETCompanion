package edu.ncsu.csc.assist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

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
        // TODO implement me
        return new LineGraphSeries<DataPoint>(new DataPoint[] {new DataPoint(0,1), new DataPoint(1,2)});
    }
}
