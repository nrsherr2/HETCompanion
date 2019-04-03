package edu.ncsu.csc.assist;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import androidx.fragment.app.Fragment;

// other detail fragments should inherit from this one
public abstract class DetailFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    GraphView graph;
    Spinner spinner;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = getView();
        graph = view.findViewById(R.id.graph);
        spinner = view.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource( getContext(),
                R.array.graph_increments, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(this);

        // initialize to live graph on creation
        setGraphData(getDataPoints("live"));
    }

    public void setGraphData(LineGraphSeries<DataPoint> series) {
         graph.addSeries(series);
    }


    public DataPoint[] arrayToDatapoints(int[] array) {
        DataPoint[] dataPoints = new DataPoint[array.length];
        for (int i = 0; i < array.length; i++) {
            dataPoints[i] = new DataPoint(i, array[i]);
        }
        return dataPoints;
    }

    /**
     * Gets the data points from the database depending on what the spinner on the view is selected.
     * @return
     */
    public abstract LineGraphSeries<DataPoint> getDataPoints(String graphView);

}
