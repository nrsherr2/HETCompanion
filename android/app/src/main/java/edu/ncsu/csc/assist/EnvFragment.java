package edu.ncsu.csc.assist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.List;

import edu.ncsu.csc.assist.data.objects.ProcessedDataType;
import edu.ncsu.csc.assist.data.objects.SummarizedData;
import edu.ncsu.csc.assist.data.sqlite.access.ProcessedDataPointDao;

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

        switch(graphView.toLowerCase())
        {
            case "live" :
                series = new LineGraphSeries<>(summaryToDatapoints(dao.querySummarizedData(ProcessedDataType.WRIST_OZ, ProcessedDataPointDao.LIVE)));
                break;
            case "minute" :
                series = new LineGraphSeries<>(summaryToDatapoints(dao.querySummarizedData(ProcessedDataType.WRIST_OZ, ProcessedDataPointDao.MINUTELY)));
                break;
            case "hour" :
                series = new LineGraphSeries<>(summaryToDatapoints(dao.querySummarizedData(ProcessedDataType.WRIST_OZ, ProcessedDataPointDao.HOURLY)));
                break;
            case "day" :
                series = new LineGraphSeries<>(summaryToDatapoints(dao.querySummarizedData(ProcessedDataType.WRIST_OZ, ProcessedDataPointDao.DAILY)));
                break;
            case "week" :
                series = new LineGraphSeries<>(summaryToDatapoints(dao.querySummarizedData(ProcessedDataType.WRIST_OZ, ProcessedDataPointDao.WEEKLY)));
                break;
            case "month" :
                series = new LineGraphSeries<>(summaryToDatapoints(dao.querySummarizedData(ProcessedDataType.WRIST_OZ, ProcessedDataPointDao.MONTHLY)));
                break;
            default :
                series = new LineGraphSeries<>(summaryToDatapoints(dao.querySummarizedData(ProcessedDataType.WRIST_OZ, ProcessedDataPointDao.LIVE)));
        }

        return series;
    }

    @Override
    public String getTitle() {
        return getString(R.string.env);
    }
}
