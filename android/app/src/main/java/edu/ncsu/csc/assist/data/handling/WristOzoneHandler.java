package edu.ncsu.csc.assist.data.handling;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import edu.ncsu.csc.assist.data.cloud.DataStorer;
import edu.ncsu.csc.assist.data.cloud.ProcessedDataStorer;
import edu.ncsu.csc.assist.data.objects.DataType;
import edu.ncsu.csc.assist.data.objects.GenericData;
import edu.ncsu.csc.assist.data.objects.ProcessedData;
import edu.ncsu.csc.assist.data.objects.ProcessedDataType;

public class WristOzoneHandler extends Handler {

    private static final int BYTES_PER_VALUE = 8;
    private static final int NUMBER_OF_VALUES = 1;
    private static final int MILLIS_BETWEEN_VALUES = 20;

    private static final int PROCESSED_DATA_MILLI_AVG = 1000;
    private List<GenericData> wristOzHistory;
    private List<GenericData> wristPozHistory;
    private List<GenericData> wristRozHistory;
    private List<GenericData> wristMozHistory;

    public WristOzoneHandler(DataStorer rawDataBuffer, ProcessedDataStorer processedDataBuffer) {
        super(BYTES_PER_VALUE, NUMBER_OF_VALUES, MILLIS_BETWEEN_VALUES, rawDataBuffer, processedDataBuffer);
        wristOzHistory = new LinkedList<>();
        wristPozHistory = new LinkedList<>();
        wristRozHistory = new LinkedList<>();
        wristMozHistory = new LinkedList<>();
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
        int ozReading = getIntFromBytes(data[0], data[1]);
        GenericData ozDataPoint = new GenericData(DataType.WRIST_OZ, ozReading, timestamp);

        int pozReading = getIntFromBytes(data[2], data[3]);
        GenericData pozDataPoint = new GenericData(DataType.WRIST_POZ, pozReading, timestamp);

        int rozReading = getIntFromBytes(data[4], data[5]);
        GenericData rozDataPoint = new GenericData(DataType.WRIST_ROZ, rozReading, timestamp);

        int mozReading = getIntFromBytes(data[6], data[7]);
        GenericData mozDataPoint = new GenericData(DataType.WRIST_MOZ, mozReading, timestamp);

        return Arrays.asList(ozDataPoint, pozDataPoint, rozDataPoint, mozDataPoint);
    }

    protected List<ProcessedData> addToHistory(List<GenericData> dataValues) {
        List<ProcessedData> averages = new LinkedList<>();
        if (dataValues.size() == 0) {
            return averages;
        }
        for (GenericData dataPoint : dataValues) {
            switch (dataPoint.getType()) {
                case DataType.WRIST_OZ:
                    wristOzHistory.add(dataPoint);
                    break;
                case DataType.WRIST_POZ:
                    wristPozHistory.add(dataPoint);
                    break;
                case DataType.WRIST_ROZ:
                    wristRozHistory.add(dataPoint);
                    break;
                case DataType.WRIST_MOZ:
                    wristMozHistory.add(dataPoint);
                    break;
            }
        }

        long latestTime = wristMozHistory.get(wristMozHistory.size() - 1).getTimestamp();
        long oldestTime = wristMozHistory.get(0).getTimestamp();
        while (latestTime - oldestTime > PROCESSED_DATA_MILLI_AVG) {
            int avgOz = 0;
            int avgPoz = 0;
            int avgRoz = 0;
            int avgMoz = 0;
            int numOz = 0;
            int numPoz = 0;
            int numRoz = 0;
            int numMoz = 0;

            while (wristOzHistory.get(0).getTimestamp() < oldestTime + PROCESSED_DATA_MILLI_AVG) {
                GenericData dataPoint = wristOzHistory.remove(0);
                avgOz += dataPoint.getValue();
                numOz++;
            }
            while (wristPozHistory.get(0).getTimestamp() < oldestTime + PROCESSED_DATA_MILLI_AVG) {
                GenericData dataPoint = wristPozHistory.remove(0);
                avgPoz += dataPoint.getValue();
                numPoz++;
            }
            while (wristRozHistory.get(0).getTimestamp() < oldestTime + PROCESSED_DATA_MILLI_AVG) {
                GenericData dataPoint = wristRozHistory.remove(0);
                avgRoz += dataPoint.getValue();
                numRoz++;
            }
            while (wristMozHistory.get(0).getTimestamp() < oldestTime + PROCESSED_DATA_MILLI_AVG) {
                GenericData dataPoint = wristMozHistory.remove(0);
                avgMoz += dataPoint.getValue();
                numMoz++;
            }

            ProcessedData averageOz = new ProcessedData(ProcessedDataType.WRIST_OZ, 1.0 * avgOz / numOz, oldestTime);
            ProcessedData averagePoz = new ProcessedData(ProcessedDataType.WRIST_POZ, 1.0 * avgPoz / numPoz, oldestTime);
            ProcessedData averageRoz = new ProcessedData(ProcessedDataType.WRIST_ROZ, 1.0 * avgRoz / numRoz, oldestTime);
            ProcessedData averageMoz = new ProcessedData(ProcessedDataType.WRIST_MOZ, 1.0 * avgMoz / numMoz, oldestTime);

            averages.add(averageOz);
            averages.add(averagePoz);
            averages.add(averageRoz);
            averages.add(averageMoz);

            if (wristOzHistory.isEmpty() || wristPozHistory.isEmpty() || wristRozHistory.isEmpty() || wristMozHistory.isEmpty()) {
                oldestTime += PROCESSED_DATA_MILLI_AVG;
                break;
            }
            oldestTime = wristMozHistory.get(0).getTimestamp();
        }
        return averages;
    }
}
