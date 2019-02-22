package edu.ncsu.csc.assist.data.cloud;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import androidx.test.core.app.ApplicationProvider;

public class RestQueueTest {

    private static final int RAW_DATA_COUNT = 100;

    private RestQueue restQueue;

    @Before
    public void setUp() throws Exception {
        restQueue = new RestQueue(ApplicationProvider.getApplicationContext());
    }

    @After
    public void tearDown() throws Exception {
        restQueue.stop();
    }

    @Test
    public void makeRequest() throws Exception {
        // Test runs async and junit misses the result/doesn't care
        /*List<RawDataPoint> data = new ArrayList<>();
        for (int i = 0; i < RAW_DATA_COUNT; i++) {
            data.add(new RawDataPoint(DataType.values()[i % DataType.values().length].getId(), System.currentTimeMillis(), ThreadLocalRandom.current().nextInt()));
        }

        restQueue.makeRequest(data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(getClass().getCanonicalName(), "Received response: ");
                for (Iterator<String> it = response.keys(); it.hasNext(); ) {
                    String key = it.next();
                    Log.d(getClass().getCanonicalName(), key);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error != null && error.networkResponse != null)
                    Log.d(getClass().getCanonicalName(), "Error: " + error.networkResponse.statusCode);
                Log.d(getClass().getCanonicalName(), "network response is null");
            }
        });*/
    }

    @Test
    public void formatJson() throws Exception {
        // TODO
    }
}