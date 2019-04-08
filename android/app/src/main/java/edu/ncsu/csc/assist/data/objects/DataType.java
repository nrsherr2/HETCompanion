package edu.ncsu.csc.assist.data.objects;

public interface DataType {

    String CHEST_BPM = "chest_bpm";
    String CHEST_HRV = "chest_hrv";
    String CHEST_ECG = "chest_ecg";
    String CHEST_PPG = "chest_ppg";
    String CHEST_INERTIA_X = "chest_inertia_x";
    String CHEST_INERTIA_Y = "chest_inertia_y";
    String CHEST_INERTIA_Z = "chest_inertia_z";
    String WRIST_INERTIA_X = "wrist_inertia_x";
    String WRIST_INERTIA_Y = "wrist_inertia_y";
    String WRIST_INERTIA_Z = "wrist_inertia_z";
    String WRIST_PPG = "wrist_ppg";
    String WRIST_OZ = "wrist_oz";
    String WRIST_POZ = "wrist_poz";
    String WRIST_ROZ = "wrist_roz";
    String WRIST_MOZ = "wrist_moz";
    String WRIST_TEMPERATURE = "wrist_temperature";
    String WRIST_HUMIDITY = "wrist_humidity";

    String[] VALUES = new String[]{"chest_ecg", "chest_ppg", "chest_inertia_x",
            "chest_inertia_y", "chest_inertia_z", "wrist_inertia_x", "wrist_inertia_y",
            "wrist_inertia_z", "wrist_ppg", "wrist_oz", "wrist_poz", "wrist_roz", "wrist_moz",
            "wrist_temperature", "wrist_humidity"
    };
}
