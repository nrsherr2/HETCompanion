package edu.ncsu.csc.assist.data.handling;

import edu.ncsu.csc.assist.data.cloud.DataStorer;
import edu.ncsu.csc.assist.data.objects.HumidityData;
import edu.ncsu.csc.assist.data.objects.TemperatureData;

public class WristEnvironmentalHandler extends Handler {

    private static final int BYTES_PER_VALUE = 4;
    private static final int NUMBER_OF_VALUES = 1;
    private static final int MILLIS_BETWEEN_VALUES = 20;

    public WristEnvironmentalHandler(DataStorer rawDataBuffer){
        super(BYTES_PER_VALUE, NUMBER_OF_VALUES, rawDataBuffer);
    }

    @Override
    public void handle(byte[] buffer, long timestamp) {
        for (int i = 0; i < getTotalByteSize(); i += getBytesPerValue()) {
            int tmpReading = getIntFromBytes(buffer[i], buffer[i + 1]);
            TemperatureData tmpDataPoint = new TemperatureData(tmpReading, timestamp + i*MILLIS_BETWEEN_VALUES);
            sendRawData(tmpDataPoint);

            int humidReading = getIntFromBytes(buffer[i + 2], buffer[i + 3]);
            HumidityData humidDataPoint = new HumidityData(humidReading, timestamp + i*MILLIS_BETWEEN_VALUES);
            sendRawData(humidDataPoint);
        }
        //send processed data
    }
}
