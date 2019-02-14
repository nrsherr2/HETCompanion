package edu.ncsu.csc.assist.data.objects;

public class ECGData {
    private int reading;
    private long timeRecorded;

    public ECGData(int reading, long timeRecorded){
        this.reading = reading;
        this.timeRecorded = timeRecorded;
    }

    public int getReading() {
        return reading;
    }

    public void setReading(int reading) {
        this.reading = reading;
    }

    public long getTimeRecorded() {
        return timeRecorded;
    }

    public void setTimeRecorded(long timeRecorded) {
        this.timeRecorded = timeRecorded;
    }
}
