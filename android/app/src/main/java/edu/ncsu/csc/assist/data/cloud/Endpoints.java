package edu.ncsu.csc.assist.data.cloud;

/**
 * Holds constants for different cloud endpoints.
 */
public class Endpoints {
    public static String DOMAIN = "http://sd-vm20.csc.ncsu.edu:8080";
    private static String API_V1 = "/api/v1";
    private static String V1_SAVE = "/save";


    public static String getV1Save() {
        return DOMAIN + API_V1 + V1_SAVE;
    }
}
