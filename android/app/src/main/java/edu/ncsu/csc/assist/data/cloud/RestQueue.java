package edu.ncsu.csc.assist.data.cloud;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ncsu.csc.assist.data.sqlite.entities.RawDataPoint;

/**
 * Maintains a queue of HTTP REST requests. The listeners are provided by the calling methods
 * to handle the async response.
 */
public class RestQueue {

    private RequestQueue queue;
    private RetryPolicy retryPolicy;

    public RestQueue(Context context) {
        // Instantiate the cache
        Cache cache = new DiskBasedCache(context.getCacheDir(), 50 * 1024 * 1024); // 50MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        queue = new RequestQueue(cache, network);

        retryPolicy = new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        queue.start();
    }

    /**
     * Set the retry policy for all future requests (excluding custom requests)
     *
     * @param retryPolicy
     */
    public void setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public Request makeRequest(Request request) {
        return queue.add(request);
    }

    public Request makeRequest(int method, String url, final Map<String, String> headers, JSONObject jsonBody, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        JsonObjectRequest request = new JsonObjectRequest(method, url, jsonBody, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };
        request.setRetryPolicy(retryPolicy);
        return makeRequest(request);
    }

    public Request sendSaveRequest(String userId, String hetVersion, String authToken, List<RawDataPoint> data, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) throws JSONException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", authToken);
        return makeRequest(Request.Method.POST, Endpoints.getV1Save(), headers, JsonUtil.formatJson(userId, hetVersion, data), listener, errorListener);
    }

    public void stop() {
        queue.stop();
    }
}
