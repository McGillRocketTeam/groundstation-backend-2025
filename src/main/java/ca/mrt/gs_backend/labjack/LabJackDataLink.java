package ca.mrt.gs_backend.labjack;

import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import libs.LJM;
import org.yamcs.TmPacket;
import org.yamcs.tctm.AbstractTmDataLink;

import java.nio.ByteBuffer;

/**
 * @author Jake
 * This class manages the connection to the LabJack, reads and writes to the LabJack
 * and places its readings into a binary packet (defined in LABJ_XTCE.xml) which is then put into a TM stream.
 *
 * At the moment, all readable pins (digital and analog) are read at every possible moment.
 */

public class LabJackDataLink extends AbstractTmDataLink implements Runnable{
    //total number of analog pins on the LabJack (T7)
    private static final int NUM_ANALOG_PINS = 14;

    //total number of digital pins on the LabJack (T7)
    private static final int NUM_DIGITAL_PINS = 23;

    //stores handle of currently connected LabJack device
    private int deviceHandle = 0;

    //stores whether the class is currently connected to a LabJack
    private boolean isConnected = false;


    /**
     * Attempts to connect to any LabJack device (via ethernet or USB)
     */
    public void attemptLabJackConnection(){
        IntByReference handleRef = new IntByReference(0);
        try{
            LJM.openS("ANY", "ANY", "ANY", handleRef);
            log.info("LabJack Connected");
            deviceHandle = handleRef.getValue();

            //Watchdog 5 min
            int type = LJM.Constants.UINT32;

            int WATCHDOG_ENABLE_DEFAULT = 61600;
            int WATCHDOG_TIMEOUT_S_DEFAULT = 61604;
            int WATCHDOG_DIO_ENABLE_DEFAULT = 61630;
            int WATCHDOG_DIO_STATE_DEFAULT = 61632;
            int WATCHDOG_RESET_ENABLE_DEFAULT = 61620;

            LJM.eWriteAddress(deviceHandle, WATCHDOG_ENABLE_DEFAULT, type, 0); //Disables watchdog to change it
            LJM.eWriteAddress(deviceHandle, WATCHDOG_TIMEOUT_S_DEFAULT, type, 300); //5 minute timer
            LJM.eWriteAddress(deviceHandle, WATCHDOG_DIO_ENABLE_DEFAULT, type, 0); //Disable DIO
            LJM.eWriteAddress(deviceHandle, WATCHDOG_DIO_STATE_DEFAULT, type, 0); //DIO all LOW
            LJM.eWriteAddress(deviceHandle, WATCHDOG_DIO_ENABLE_DEFAULT, type, 1); //Re enable DIO
            LJM.eWriteAddress(deviceHandle, WATCHDOG_ENABLE_DEFAULT, type, 1); //Re-enable watchdog

            isConnected = true;

        } catch(Exception e){
            log.warn("Could not connect to LabJack");
        }

    }

    /**
     * Reads all readable LabJack pins (analog, digital) and packs the readings into a binary packet according to
     * LABJ_XTCE.xml where all analog data is first followed by all digital data.
     * @return a TmPacket containing the constructed binary packet
     */
    private TmPacket readAllPins(){
        if(!isConnected){
            log.error("Attempting to read LabJack pins when no LabJack is connected");
            throw new IllegalStateException();
        }

        double[] analogReadings = new double[NUM_ANALOG_PINS];

        for(int i = 0; i < NUM_ANALOG_PINS; i++){
            analogReadings[i] = readAnalogPin(i);
        }
        byte[] analogBinaryData = createAnalogBinaryPacket(analogReadings);


        int[] digitalReadings = new int[NUM_DIGITAL_PINS];

        for(int i = 0; i < NUM_DIGITAL_PINS; i++){
            digitalReadings[i] = readDigitalPin(i);
        }
        byte[] digitalBinaryData = createDigitalBinaryPacket(digitalReadings);


        byte[] combinedBinaryData = new byte[analogBinaryData.length + digitalBinaryData.length];
        int index = 0;
        for(; index < analogBinaryData.length; index++){
            combinedBinaryData[index] = analogBinaryData[index];
        }
        for(; index < combinedBinaryData.length; index++){
            combinedBinaryData[index] = digitalBinaryData[index-analogBinaryData.length];
        }
        TmPacket tmPacket = new TmPacket(timeService.getMissionTime(), combinedBinaryData);
        updateStats(combinedBinaryData.length);
        return packetPreprocessor.process(tmPacket);
    }

    /**
     * Takes an array of digital pin readings and converts it into an array of bytes corresponding to the reading.
     * Note that each element in the incoming pinValues array is converted into a 2-bit integer where:
     * 0 - corresponds to LOW
     * 1 - corresponds to HIGH
     * 2 - corresponds to UNKNOWN
     * @param pinValues array of digital pin readings
     * @return array of bytes corresponding to the incoming array of ints, just converted to 2-bits each
     */
    private byte[] createDigitalBinaryPacket(int[] pinValues) {
        int totalBits = pinValues.length * 2; // Each value is 2 bits
        int byteCount = (totalBits + 7) / 8;  // Calculate the number of bytes needed (round up)
        byte[] packet = new byte[byteCount];  // Byte array to hold the packed bits

        int bitPosition = 0;  // Tracks where in the byte array to place the next value

        for (int value : pinValues) {
            if (value < 0 || value > 3) {
                throw new IllegalArgumentException("Pin values must be between 0 and 3 (2-bit value).");
            }

            // Calculate which byte and bit to place the value in
            int byteIndex = bitPosition / 8;
            int bitOffset = bitPosition % 8;

            // Pack the 2-bit value into the correct position in the byte array
            packet[byteIndex] |= (byte) ((value & 0x03) << (6 - bitOffset));

            bitPosition += 2; // Move to the next 2 bits
        }

        return packet;
    }

    /**
     * Converts array of floating point analog readings into a corresponding array of bytes.
     * @param floatValues array of analog readings
     * @return array of bytes corresponding to the incoming array of readings (just used Float.floatToIntBits)
     */
    private byte[] createAnalogBinaryPacket(double[] floatValues) {
        ByteBuffer buffer = ByteBuffer.allocate(floatValues.length * 4); // Each float is 4 bytes (32 bits)

        for (double value : floatValues) {
            int bits = Float.floatToIntBits((float) value);  // Convert float to 32-bit int representation
            buffer.putInt(bits);  // Add the 32-bit int to the byte buffer
        }

        return buffer.array();  // Return the packed byte array
    }

    /**
     * Reads a single analog pin
     * @param address address of pin to read (MUST BE BETWEEN 0-13 inclusive)
     * @return value read from analog pin, Double.NaN if error occurred
     */
    private double readAnalogPin(int address){
        DoubleByReference valueRef = new DoubleByReference(0);
        int base_address = 0;
        try{
            LJM.eReadAddress(deviceHandle, base_address + address*2, LJM.Constants.FLOAT32, valueRef);
        } catch(Exception e){
            log.error("Could not read from analog pin: " + (base_address + address * 2));
            return Double.NaN;
        }
        return valueRef.getValue();
    }

    /**
     * Reads a digital pin on the LabJack. NOTE: this method must return a value that can fit within 2 bits
     * or else the above the createDigitalBinaryPacket method will break.
     * @param address address of the digital pin (0-22)
     * @return 0 if reading is low, 1 if reading is high and 2 if unknown reading
     * @see #createDigitalBinaryPacket
     */
    public int readDigitalPin(int address){
        DoubleByReference valueRef = new DoubleByReference(0);
        int base_address = 2000;
        int type = LJM.Constants.UINT16;
        try{
            LJM.eReadAddress(deviceHandle, base_address + address, type, valueRef);
        } catch(Exception e){
            log.error("Could not read from digital pin: " + (base_address + address * 2));
            return 2;
        }
        return valueRef.getValue() >= 0.5 ? 1 : 0;
    }


    @Override
    protected Status connectionStatus() {
        return isConnected ? Status.OK : Status.UNAVAIL;
    }

    @Override
    protected void doStart() {
        if (!isDisabled()) {
            Thread thread = new Thread(this);
            thread.setName(getClass().getSimpleName());
            thread.start();
        }
        notifyStarted();
    }

    @Override
    protected void doStop() {
        if(isConnected){
            LJM.close(deviceHandle);
        }
        notifyStopped();
    }

    @Override
    public void run() {
        while (isRunningAndEnabled()){
            if(isConnected){
                processPacket(readAllPins());
            }else {
                attemptLabJackConnection();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

    @Override
    public void doDisable() {
        if (isConnected) {
            LJM.close(deviceHandle);
        }
    }

    @Override
    public String getDetailedStatus() {
        if (isDisabled()) {
            return "DISABLED";
        } else if(isConnected){
            return "OK, connected to LabJack";
        } else {
            return "UNAVAILABLE, not connected to LabJack";
        }
    }
}