package com.example.myproject.labjack;

import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import libs.LJM;
import org.yamcs.TmPacket;
import org.yamcs.tctm.AbstractTmDataLink;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class LabJackDataLink extends AbstractTmDataLink implements Runnable{
    private int deviceHandle = 0;
    private boolean isConnected = false;
    private static final int NUMER_OF_ANALOG_PINS = 14;
    private final Set<Integer> digitalPinsToReadFrom = new HashSet<>();

    public void attemptLabJackConnection(){
        IntByReference handleRef = new IntByReference(0);
        try{
            LJM.openS("ANY", "ANY", "ANY", handleRef);
            System.out.println("LabJack Connected");
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


    private TmPacket readAllPins(){
        if(!isConnected){
            log.error("Attempting to read LabJack pins when no LabJack is connected");
            throw new IllegalStateException();
        }

        double[] analogReadings = new double[NUMER_OF_ANALOG_PINS];

        for(int i = 0; i < NUMER_OF_ANALOG_PINS; i++){
            analogReadings[i] = readAnalogPin(i);
        }

        Boolean[] digitalReadings = new Boolean[digitalPinsToReadFrom.size()];

        for(int i = 0; i < digitalPinsToReadFrom.size(); i++){
            digitalReadings[i] = readDigitalPin(i);
        }

        return null;
    }

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

    public Boolean readDigitalPin(int address){
        DoubleByReference valueRef = new DoubleByReference(0);
        int base_address = 2000;
        int type = LJM.Constants.UINT16;
        try{
            LJM.eReadAddress(deviceHandle, base_address + address, type, valueRef);
        } catch(Exception e){
            log.error("Could not read from digital pin: " + (base_address + address * 2));
            return null;
        }
        return valueRef.getValue() >= 0.5;
    }


    @Override
    protected Status connectionStatus() {
        return isConnected ? Status.OK : Status.UNAVAIL;
    }

    @Override
    protected void doStart() {

    }

    @Override
    protected void doStop() {

    }

    @Override
    public void run() {
        while (isRunningAndEnabled()){
            if(isConnected){

            }
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
