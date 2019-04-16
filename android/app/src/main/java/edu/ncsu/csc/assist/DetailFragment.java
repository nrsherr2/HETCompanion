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

import java.util.List;

import androidx.fragment.app.Fragment;
import edu.ncsu.csc.assist.data.objects.SummarizedData;
import edu.ncsu.csc.assist.data.sqlite.AppDatabase;
import edu.ncsu.csc.assist.data.sqlite.access.ProcessedDataPointDao;

// other detail fragments should inherit from this one
public abstract class DetailFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    GraphView graph;
    Spinner spinner;
    TextView title;
    ProcessedDataPointDao dao;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = getView();

        AppDatabase db = AppDatabase.getDatabase(getActivity().getApplication());
        dao = db.processedDataPointDao();

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
     *
     * @param summarizedData
     * @return array of Datapoints
     */
    public DataPoint[] summaryToDatapoints(List<SummarizedData> summarizedData) {
        if (summarizedData.isEmpty()) {
            return new DataPoint[0];
        }

        // TODO: change from 0,1,2,3,etc to date/time labels
        DataPoint[] dataPoints = new DataPoint[summarizedData.size()];
        for (int i = 0; i < summarizedData.size(); i++) {
            dataPoints[i] = new DataPoint(i, summarizedData.get(i).value);
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
