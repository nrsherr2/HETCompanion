package edu.ncsu.csc.assist.data.cloud;

/**
 * Holds constants for different cloud endpoints.
 */
public interface Endpoints {
    //TODO replace this endpoint with real domain
    String DOMAIN = "http://sd-vm20.csc.ncsu.edu:8080";
    String API_V1 = DOMAIN + "/api/v1";
    String V1_SAVE = API_V1 + "/save";
}
