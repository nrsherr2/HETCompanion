package edu.ncsu.csc.assist.data.handling;

import java.nio.ByteBuffer;

public class ECGHandler {

    private static final int BYTES_PER_VALUE = 3;
    /**
     * Expects 12 bytes of ecg data sent from the HET Chest device
     * Each 3 bytes are one reading and are generated 5ms apart
     * @param buffer 12 bytes of ecg data
     */
    public static void handle(byte[] buffer, long timestamp){
      int[] ecgReadings = new int[buffer.length/BYTES_PER_VALUE];
      for(int i = 0; i < buffer.length/BYTES_PER_VALUE; i +=BYTES_PER_VALUE){
          byte[] dataBytes = {0,buffer[i],buffer[i+1],buffer[i+2]};
          ByteBuffer wrapped = ByteBuffer.wrap(dataBytes);
          int reading = wrapped.getInt();
          ecgReadings[i] = reading;
          sendRawData(reading,timestamp + i*5);
      }
      double heartRate = determineHeartRate();
      sendProcessedData(heartRate, timestamp);
    }

    public static double determineHeartRate(){
        return 0;
    }

    /**
     * Sends raw data to the raw data database buffer
     * @param ecgReading readings of ecg data
     * @param timestamp the time that the reading was recorded
     */
    public static void sendRawData(int ecgReading, long timestamp){

    }

    /**
     * Sends processed data to the processed database buffer
     * -processed data in this case means estimated heart rate
     * @param heartrate calculated heart rate from the ecg data
     * @param timestamp the time that the heart rate was recorded
     */
    public static void sendProcessedData(double heartrate, long timestamp){

    }

}
