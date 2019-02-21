package edu.ncsu.csc.assist.data.objects;

public enum DataType {

    CHEST_ECG("chest_ecg"),
    CHEST_PPG("chest_ppg"),
    CHEST_INERTIA_X("chest_inertia_x"),
    CHEST_INERTIA_Y("chest_inertia_y"),
    CHEST_INERTIA_Z("chest_inertia_z"),
    WRIST_INERTIA_X("wrist_inertia_x"),
    WRIST_INERTIA_Y("wrist_inertia_y"),
    WRIST_INERTIA_Z("wrist_inertia_z"),
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
