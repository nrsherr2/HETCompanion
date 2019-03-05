package edu.ncsu.csc.assist.data.cloud;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import edu.ncsu.csc.assist.data.sqlite.entities.RawDataPoint;

/**
 * Convert raw data points to JSON for uploading data to the cloud
 */
public class JsonUtil {

    private static final SortByTimestamp SORT_BY_TIMESTAMP = new SortByTimestamp();

    /**
     * Format given list to JSON structure for REST API
     * Compatible with API v1
     *
     * @param data
     * @return
     * @throws JSONException
     */
    public static JSONObject formatJson(@NonNull String userId, @NonNull String hetVersion, @NonNull List<RawDataPoint> data) throws JSONException {
        JSONObject json = new JSONObject();

        json.put("user_id", userId);
        json.put("het_version", hetVersion);

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
            json.put(dataType, generateSubJson(filteredData));
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
    private static JSONObject generateSubJson(List<RawDataPoint> rawDataPoints) throws JSONException {
        JSONObject json = new JSONObject();

        // Sort the received data by timestamp
        Collections.sort(rawDataPoints, SORT_BY_TIMESTAMP);

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
        json.put("delta", new JSONArray(delta));
        json.put("data", new JSONArray(data));
        return json;
    }


    /**
     * Comparator for sorting lists of raw data by timestamp
     */
    private static class SortByTimestamp implements Comparator<RawDataPoint> {
        public int compare(RawDataPoint o1, RawDataPoint o2) {
            return Long.compare(o1.getTimestamp(), o2.getTimestamp());
        }
    }
}
