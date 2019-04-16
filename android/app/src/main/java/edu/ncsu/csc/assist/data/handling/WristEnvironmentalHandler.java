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

public class WristEnvironmentalHandler extends Handler {

    private static final int BYTES_PER_VALUE = 4;
    private static final int NUMBER_OF_VALUES = 1;
    private static final int MILLIS_BETWEEN_VALUES = 20;

    private static final int PROCESSED_DATA_MILLI_AVG = 1000;
    private List<GenericData> wristTemperatureHistory;
    private List<GenericData> wristHumidHistory;

    public WristEnvironmentalHandler(DataStorer rawDataBuffer, ProcessedDataStorer processedDataBuffer) {
        super(BYTES_PER_VALUE, NUMBER_OF_VALUES, MILLIS_BETWEEN_VALUES, rawDataBuffer, processedDataBuffer);
        wristTemperatureHistory = new LinkedList<>();
        wristHumidHistory = new LinkedList<>();
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
        int temperatureReading = getIntFromBytes(data[0], data[1]);
        GenericData temperatureDataPoint = new GenericData(DataType.WRIST_TEMPERATURE, temperatureReading, timestamp);

        int humidReading = getIntFromBytes(data[2], data[3]);
        GenericData humidDataPoint = new GenericData(DataType.WRIST_HUMIDITY, humidReading, timestamp);

        return Arrays.asList(temperatureDataPoint, humidDataPoint);
    }

    protected List<ProcessedData> addToHistory(List<GenericData> dataValues) {
        List<ProcessedData> averages = new LinkedList<>();
        if (dataValues.size() == 0) {
            return averages;
        }
        for (GenericData dataPoint : dataValues) {
            switch (dataPoint.getType()) {
                case DataType.WRIST_TEMPERATURE:
                    wristTemperatureHistory.add(dataPoint);
                    break;
                case DataType.WRIST_HUMIDITY:
                    wristHumidHistory.add(dataPoint);
                    break;
            }
        }

        long latestTime = wristHumidHistory.get(wristHumidHistory.size() - 1).getTimestamp();
        long oldestTime = wristHumidHistory.get(0).getTimestamp();
        while (latestTime - oldestTime > PROCESSED_DATA_MILLI_AVG) {
            int avgTemperature = 0;
            int avgHumid = 0;
            int numTemperature = 0;
            int numHumid = 0;

            while (wristTemperatureHistory.get(0).getTimestamp() < oldestTime + PROCESSED_DATA_MILLI_AVG) {
                GenericData dataPoint = wristTemperatureHistory.remove(0);
                avgTemperature += dataPoint.getValue();
                numTemperature++;
            }
            while (wristHumidHistory.get(0).getTimestamp() < oldestTime + PROCESSED_DATA_MILLI_AVG) {
                GenericData dataPoint = wristHumidHistory.remove(0);
                avgHumid += dataPoint.getValue();
                numHumid++;
            }

            ProcessedData averageTemperature = new ProcessedData(ProcessedDataType.WRIST_TEMPERATURE, 1.0 * avgTemperature / numTemperature, (latestTime + oldestTime) / 2);
            ProcessedData averageHumid = new ProcessedData(ProcessedDataType.WRIST_HUMIDITY, 1.0 * avgHumid / numHumid, oldestTime);

            averages.add(averageTemperature);
            averages.add(averageHumid);

            if (wristTemperatureHistory.isEmpty() || wristHumidHistory.isEmpty()) {
                break;
            }
            oldestTime = wristHumidHistory.get(0).getTimestamp();
        }
        return averages;
    }
}
