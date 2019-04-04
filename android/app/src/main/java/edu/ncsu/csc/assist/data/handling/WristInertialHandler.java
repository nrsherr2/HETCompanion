package edu.ncsu.csc.assist.data.handling;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import edu.ncsu.csc.assist.data.cloud.DataStorer;
import edu.ncsu.csc.assist.data.cloud.ProcessedDataStorer;
import edu.ncsu.csc.assist.data.objects.DataType;
import edu.ncsu.csc.assist.data.objects.GenericData;
import edu.ncsu.csc.assist.data.objects.ProcessedData;

public class WristInertialHandler extends Handler {

    private static final int BYTES_PER_VALUE = 6;
    private static final int NUMBER_OF_VALUES = 2;
    private static final int MILLIS_BETWEEN_VALUES = 10;

    private static final int PROCESSED_DATA_MILLI_AVG = 1000;
    private List<GenericData> wristInertialXHistory;
    private List<GenericData> wristInertialYHistory;
    private List<GenericData> wristInertialZHistory;

    public WristInertialHandler(DataStorer rawDataBuffer, ProcessedDataStorer processedDataBuffer) {
        super(BYTES_PER_VALUE, NUMBER_OF_VALUES, MILLIS_BETWEEN_VALUES, rawDataBuffer, processedDataBuffer);
        wristInertialXHistory = new LinkedList<>();
        wristInertialYHistory = new LinkedList<>();
        wristInertialZHistory = new LinkedList<>();
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
        int xReading = getIntFromBytes(data[0], data[1]);
        GenericData xDataPoint = new GenericData(DataType.WRIST_INERTIA_X, xReading, timestamp);

        int yReading = getIntFromBytes(data[2], data[3]);
        GenericData yDataPoint = new GenericData(DataType.WRIST_INERTIA_Y, yReading, timestamp);

        int zReading = getIntFromBytes(data[4], data[5]);
        GenericData zDataPoint = new GenericData(DataType.WRIST_INERTIA_Z, zReading, timestamp);

        return  Arrays.asList(xDataPoint,yDataPoint,zDataPoint);
    }

    protected List<ProcessedData> addToHistory(List<GenericData> dataValues) {
        List<ProcessedData> averages = new LinkedList<>();
        if (dataValues.size() == 0) {
            return averages;
        }
        for (GenericData dataPoint : dataValues) {
            switch (dataPoint.getType()) {
                case DataType.WRIST_INERTIA_X:
                    wristInertialXHistory.add(dataPoint);
                    break;
                case DataType.WRIST_INERTIA_Y:
                    wristInertialYHistory.add(dataPoint);
                    break;
                case DataType.WRIST_INERTIA_Z:
                    wristInertialZHistory.add(dataPoint);
                    break;
            }
        }

        long latestTime = wristInertialZHistory.get(wristInertialZHistory.size() - 1).getTimestamp();
        long oldestTime = wristInertialZHistory.get(0).getTimestamp();
        while (latestTime - oldestTime > PROCESSED_DATA_MILLI_AVG) {
            int avgInertialX = 0;
            int avgInertialY = 0;
            int avgInertialZ = 0;
            int numX = 0;
            int numY = 0;
            int numZ = 0;

            while (wristInertialXHistory.get(0).getTimestamp() < oldestTime + PROCESSED_DATA_MILLI_AVG) {
                GenericData dataPoint = wristInertialXHistory.remove(0);
                avgInertialX += dataPoint.getValue();
                numX++;
            }
            while (wristInertialYHistory.get(0).getTimestamp() < oldestTime + PROCESSED_DATA_MILLI_AVG) {
                GenericData dataPoint = wristInertialYHistory.remove(0);
                avgInertialY += dataPoint.getValue();
                numY++;
            }
            while (wristInertialZHistory.get(0).getTimestamp() < oldestTime + PROCESSED_DATA_MILLI_AVG) {
                GenericData dataPoint = wristInertialZHistory.remove(0);
                avgInertialZ += dataPoint.getValue();
                numZ++;
            }

            ProcessedData averageX = new ProcessedData(DataType.WRIST_INERTIA_X, 1.0 * avgInertialX / numX, oldestTime);
            ProcessedData averageY = new ProcessedData(DataType.WRIST_INERTIA_Y, 1.0 * avgInertialY / numY,  oldestTime);
            ProcessedData averageZ = new ProcessedData(DataType.WRIST_INERTIA_Z, 1.0 * avgInertialZ / numZ, oldestTime);

            averages.add(averageX);
            averages.add(averageY);
            averages.add(averageZ);

            if (wristInertialXHistory.isEmpty() || wristInertialYHistory.isEmpty() || wristInertialZHistory.isEmpty()) {
                break;
            }
            oldestTime = wristInertialZHistory.get(0).getTimestamp();
        }
        return averages;
    }
}
