package edu.ncsu.csc.assist.data.objects;

public enum DataType {

    CHEST_ECG("chest_ecg"),
    CHEST_PPG("chest_ppg"),
    CHEST_INERTIA("chest_inertia"),
    WRIST_INERTIA("wrist_inertia"),
    WRIST_PPG("wrist_ppg"),
    WRIST_OZ("wrist_oz"),
    WRIST_POZ("wrist_poz"),
    WRIST_ROZ("wrist_roz"),
    WRIST_MOZ("wrist_moz"),
    WRIST_TEMPERATURE("wrist_temperature"),
    WRIST_HUMIDITY("wrist_humidity");


    private String id;

    DataType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

}
