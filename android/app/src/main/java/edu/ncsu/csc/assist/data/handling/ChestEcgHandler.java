package edu.ncsu.csc.assist.data.handling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import edu.ncsu.csc.assist.data.cloud.DataStorer;
import edu.ncsu.csc.assist.data.cloud.ProcessedDataStorer;
import edu.ncsu.csc.assist.data.objects.DataType;
import edu.ncsu.csc.assist.data.objects.GenericData;
import edu.ncsu.csc.assist.data.objects.ProcessedData;
import edu.ncsu.csc.assist.data.objects.ProcessedDataType;

public class ChestEcgHandler extends Handler {

    private static final int BYTES_PER_VALUE = 3;
    private static final int NUMBER_OF_VALUES = 4;
    private static final int MILLIS_BETWEEN_VALUES = 5;

    private List<GenericData> ecgHistory = new LinkedList<GenericData>();
    private final int HISTORY_SIZE = 10000;
    private final int PROCESS_PERIOD = 1000;
    private long timeLastProcessed = 0;

    public ChestEcgHandler(DataStorer rawDataBuffer, ProcessedDataStorer processedDataBuffer) {
        super(BYTES_PER_VALUE, NUMBER_OF_VALUES, MILLIS_BETWEEN_VALUES, rawDataBuffer, processedDataBuffer);
        timeLastProcessed = System.currentTimeMillis() + HISTORY_SIZE;
    }

    /**
     * Expects 12 bytes of ecg data sent from the HET Chest device
     * Each 3 bytes are one reading and are generated 5ms apart
     * This method should run approximately every 20 ms
     *
     * @param buffer    12 bytes of ecg data
     * @param timestamp the time that the first data value in the buffer was read
     */
    @Override
    public void handle(byte[] buffer, long timestamp) {
        List<GenericData> dataValues = parseInput(buffer, timestamp);
        sendRawData(dataValues);

        ecgHistory.addAll(dataValues);
        while(!ecgHistory.isEmpty() && ecgHistory.get(0).getTimestamp() < timestamp - HISTORY_SIZE){
            ecgHistory.remove(0);
        }
        if(timestamp - timeLastProcessed >= PROCESS_PERIOD){
            List<ProcessedData> heartData = calculateHeartData();
            if(heartData.size() == 2){
                sendProcessedData(heartData);
            }
            timeLastProcessed = timestamp;
        }
    }

    @Override
    protected List<GenericData> parseReading(byte[] data, long timestamp) {
        int reading = getIntFromBytes(data);
        GenericData dataPoint = new GenericData(DataType.CHEST_ECG, reading, timestamp);
        return Collections.singletonList(dataPoint);
    }

    /**
     * performs analysis on the ecg data to determine bpm and HRV
     *
     * @return a List of size 2.
     *  -index 0 is heart beats per minute
     *  -index 1 is heart rate variability
     */
    private List<ProcessedData> calculateHeartData() {
        List<ProcessedData> heartData = new ArrayList<>();
        if(ecgHistory.isEmpty()){
            return heartData;
        }

        /**
         * Calculates BPM from the past HISTORY_SIZE milliseconds
         * Process:
         * 1)Find the maximum value of the past X many ecg readings (maxValue)
         * 2)Set a minimum threshold to determine what readings are indicative of a heart beat (beatThreshold)
         *      -> some alpha*maxValue,   where alpha = 0 .. 1.0
         * 3)record the first reading in every group that surpasses the threshold (reading > beatThreshold)
         * 4)calculate # of groupings * (60000)/HISTORY_SIZE to determine estimate beats per 1 minute.
         *      -> where 60000 is the number of milliseconds in a minute
         *
         */

        //Step 1
        double maxValue = 0;
        for(GenericData reading : ecgHistory) {
            if(reading.getValue() > maxValue){
                maxValue = reading.getValue();
            }
        }
        //Step 2
        final double beatThreshold = maxValue * .75;
        //Step 3
        boolean alreadyInPeak = false;
        List<GenericData> heartBeats = new ArrayList<>();
        for(GenericData reading : ecgHistory){
            if(reading.getValue() >= beatThreshold){
                if(alreadyInPeak == false){
                    heartBeats.add(reading);
                    alreadyInPeak = true;
                }
            } else{
                alreadyInPeak = false;
            }
        }
        //Step 4
        double bpm = heartBeats.size() *  (60000.0/HISTORY_SIZE);
        heartData.add(new ProcessedData(ProcessedDataType.HEARTRATE, bpm, ecgHistory.get(0).getTimestamp()));

        /**
         * Calculates HRV for the past HISTORY_SIZE milliseconds (Root Mean Square of Successive Differences a.k.a RMSSD)
         * Process:
         * 1)Record the time difference between successive R peaks
         * 2)Perform RMSSD
         *   -RMSSD = Square root( Average( Each R-R Difference^2 ) )
         *
         */
        //Step 1
        List<Long> rrDiff = new ArrayList<>();
        for(int i = 0; i < heartBeats.size() - 1; i++) {
            rrDiff.add(heartBeats.get(i + 1).getTimestamp() - heartBeats.get(i).getTimestamp());
        }

        //Step 2
        double rootMeanSquareSum = 0;
        for(int i = 0; i < rrDiff.size() - 1; i++){
            rootMeanSquareSum += Math.pow(rrDiff.get(i + 1) - rrDiff.get(i), 2);
        }

        double rootMeanSquare = Math.sqrt(rootMeanSquareSum/(rrDiff.size()-1));
        heartData.add(new ProcessedData(ProcessedDataType.HRV, rootMeanSquare, ecgHistory.get(0).getTimestamp()));

        return heartData;
    }

}
