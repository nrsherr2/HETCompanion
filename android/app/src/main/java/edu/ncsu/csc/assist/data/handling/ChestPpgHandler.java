package edu.ncsu.csc.assist.data.handling;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import edu.ncsu.csc.assist.data.cloud.DataStorer;
import edu.ncsu.csc.assist.data.cloud.ProcessedDataStorer;
import edu.ncsu.csc.assist.data.objects.DataType;
import edu.ncsu.csc.assist.data.objects.GenericData;
import edu.ncsu.csc.assist.data.objects.ProcessedData;
import edu.ncsu.csc.assist.data.objects.ProcessedDataType;

public class ChestPpgHandler extends Handler {

    private static final int BYTES_PER_VALUE = 2;
    private static final int NUMBER_OF_VALUES = 4;
    private static final int MILLIS_BETWEEN_VALUES = 5;

    private static final int PROCESSED_DATA_MILLI_AVG = 1000;
    private List<GenericData> chestPpgHistory;

    public ChestPpgHandler(DataStorer rawDataBuffer, ProcessedDataStorer processedDataBuffer) {
        super(BYTES_PER_VALUE, NUMBER_OF_VALUES, MILLIS_BETWEEN_VALUES, rawDataBuffer, processedDataBuffer);
        chestPpgHistory = new LinkedList<GenericData>();
    }

    @Override
    public void handle(byte[] buffer, long timestamp) {
        List<GenericData> dataValues = parseInput(buffer, timestamp);
        sendRawData(dataValues);
        List<ProcessedData> averages = addToHistory(dataValues);
        if (!averages.isEmpty()) {
            sendProcessedData(averages);
        }
    }

    @Override
    protected List<GenericData> parseReading(byte[] data, long timestamp) {
        int reading = getIntFromBytes(data[0], data[1]);
        GenericData dataPoint = new GenericData(DataType.CHEST_PPG, reading, timestamp);
        return Collections.singletonList(dataPoint);
    }

    protected List<ProcessedData> addToHistory(List<GenericData> dataValues) {
        List<ProcessedData> averages = new LinkedList<>();
        if (dataValues.size() == 0) {
            return averages;
        }

        chestPpgHistory.addAll(dataValues);
        long latestTime = chestPpgHistory.get(chestPpgHistory.size() - 1).getTimestamp();
        long oldestTime = chestPpgHistory.get(0).getTimestamp();
        while (latestTime - oldestTime > PROCESSED_DATA_MILLI_AVG) {
            int avgPpg = 0;
            int numValues = 0;
            while (chestPpgHistory.get(0).getTimestamp() < oldestTime + PROCESSED_DATA_MILLI_AVG) {
                GenericData dataPoint = chestPpgHistory.remove(0);
                avgPpg += dataPoint.getValue();
                numValues++;
            }
            ProcessedData average = new ProcessedData(ProcessedDataType.CHEST_PPG, 1.0 * avgPpg / numValues, oldestTime);
            averages.add(average);

            if (chestPpgHistory.isEmpty()) {
                break;
            }
            oldestTime = chestPpgHistory.get(0).getTimestamp();
        }
        return averages;
    }
}
