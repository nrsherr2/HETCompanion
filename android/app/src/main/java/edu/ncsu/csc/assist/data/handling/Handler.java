package edu.ncsu.csc.assist.data.handling;

public abstract class Handler {

    protected int bytesPerValue;

    protected int numberOfValues;

    public Handler(int bytesPerValue, int numberOfValues) {
        this.bytesPerValue = bytesPerValue;
        this.numberOfValues = numberOfValues;
    }


    public abstract void handle(byte[] buffer, long timestamp);

    public int getTotalByteSize(){
        return bytesPerValue * numberOfValues;
    }

    public int getBytesPerValue() {
        return bytesPerValue;
    }

    public int getNumberOfValues() {
        return numberOfValues;
    }

}
