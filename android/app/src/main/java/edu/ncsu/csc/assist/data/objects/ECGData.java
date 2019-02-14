package edu.ncsu.csc.assist.data.objects;

public class ECGData {
    private int reading;
    private long timestamp;

    public ECGData(int reading, long timestamp){
        this.reading = reading;
        this.timestamp = timestamp;
    }

    public int getReading() {
        return reading;
    }

    public void setReading(int reading) {
        this.reading = reading;
    }

    public long getTimeRecorded() {
        return timestamp;
    }

    public void setTimeRecorded(long timestamp) {
        this.timestamp = timestamp;
    }
}
