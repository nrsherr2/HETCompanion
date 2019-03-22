package edu.ncsu.csc.assist;

import com.google.android.gms.common.api.GoogleApiClient;

public class SignInClientHolder {

    private static SignInClientHolder INSTANCE;

    private GoogleApiClient client;

    public static GoogleApiClient getClient() {
        if (INSTANCE == null) {
            INSTANCE = new SignInClientHolder();
        }
        return INSTANCE.client;
    }

    public static void setClient(GoogleApiClient client) {
        if (INSTANCE == null) {
            INSTANCE = new SignInClientHolder();
        }
        INSTANCE.client = client;
    }
}
