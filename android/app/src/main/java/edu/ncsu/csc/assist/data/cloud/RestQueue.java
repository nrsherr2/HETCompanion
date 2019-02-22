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

import java.util.List;

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
        //TODO transform data into JSON
        return new JSONObject("{size: " + data.size() + "}");
    }

    String getEndpointUrl() {
        return ENDPOINT_URL;
    }

}
