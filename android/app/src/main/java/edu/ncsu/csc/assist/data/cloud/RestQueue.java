package edu.ncsu.csc.assist.data.cloud;

import android.content.Context;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.ncsu.csc.assist.data.sqlite.entities.RawDataPoint;

public class RestQueue {

    private static final String ENDPOINT_URL = "http://localhost/api/v1/save";

    private RequestQueue queue;

    public RestQueue(Context context) {
        // Instantiate the cache
        Cache cache = new DiskBasedCache(context.getCacheDir(), 50 * 1024 * 1024); // 50MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        queue = new RequestQueue(cache, network);

        queue.start();
    }

    public void makeRequest(List<RawDataPoint> data, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) throws JSONException {
        queue.add(new JsonObjectRequest(Request.Method.POST, getEndpointUrl(), formatJson(data), listener, errorListener));
    }

    public void stop() {
        queue.stop();
    }


    protected static JSONObject formatJson(List<RawDataPoint> data) throws JSONException {
        JSONObject json = new JSONObject();

        // Get a list of the unique data types to save
        Set<String> dataTypes = new HashSet<>();
        for (RawDataPoint raw : data) {
            dataTypes.add(raw.getType());
        }

        // Generate sub objects for each type
        for (String dataType : dataTypes) {
            List<RawDataPoint> filteredData = new ArrayList<>();
            for (RawDataPoint raw : data) {
                if (dataType.equals(raw.getType()))
                    filteredData.add(raw);
            }
            json.put(dataType, genereateSubJson(filteredData));
        }
        return json;
    }

    /**
     * Generate the subsection of JSON that contains the initial timestamp, delta timestamps, and data arrays
     *
     * @param rawDataPoints Data all of a single type that you want to be compressed into the above format.
     * @return
     * @throws JSONException
     */
    protected static JSONObject genereateSubJson(List<RawDataPoint> rawDataPoints) throws JSONException {
        if (rawDataPoints.size() <= 0) {
            return null;
        }

        JSONObject json = new JSONObject();

        // Sort the received data by timestamp
        Collections.sort(rawDataPoints, new SortByTimestamp());

        // Find the lowest timestamp in the given data
        long initialTimestamp = rawDataPoints.get(0).getTimestamp();
        json.put("initial_timestamp", initialTimestamp);

        long[] delta = new long[rawDataPoints.size() - 1];
        int[] data = new int[rawDataPoints.size()];

        long lastTimeStamp = initialTimestamp;
        data[0] = rawDataPoints.get(0).getValue();
        for (int i = 1; i < rawDataPoints.size(); i++) {
            data[i] = rawDataPoints.get(i).getValue();
            delta[i - 1] = rawDataPoints.get(i).getTimestamp() - lastTimeStamp;
            lastTimeStamp = rawDataPoints.get(i).getTimestamp();
        }
        json.put("delta", delta);
        json.put("data", data);
        return json;
    }


    private static class SortByTimestamp implements Comparator<RawDataPoint> {
        public int compare(RawDataPoint o1, RawDataPoint o2) {
            return Long.compare(o1.getTimestamp(), o2.getTimestamp());
        }
    }




    String getEndpointUrl() {
        return ENDPOINT_URL;
    }

}
