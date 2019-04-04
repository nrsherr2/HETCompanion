package edu.ncsu.csc.assist;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import androidx.fragment.app.Fragment;

// other detail fragments should inherit from this one
public abstract class DetailFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    GraphView graph;
    Spinner spinner;
    TextView title;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = getView();

        // set the title
        title = view.findViewById(R.id.detail_title);
        title.setText(getTitle());

        graph = view.findViewById(R.id.graph);
        spinner = view.findViewById(R.id.spinner);
        // set the drop down spinner options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource( getContext(),
                R.array.graph_increments, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(this);

        // initialize to live graph on creation
        setGraphData(getDataPoints("Live"));
    }

    /**
     * Sets the graph view to the series passed in
     * @param series the data set to display on the graph
     */
    public void setGraphData(LineGraphSeries<DataPoint> series) {
         graph.removeAllSeries();
         graph.addSeries(series);
    }

    /**
     * Converts an array of doubles to an array of datapoints for the graph
     * @param array of doubles IN ORDER that the graph should display
     * @return array of Datapoints
     */
    public DataPoint[] arrayToDatapoints(double[] array) {
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

    /**
     * Returns the title of the fragment being shown
     * @return a string for the title
     */
    public abstract String getTitle();

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
