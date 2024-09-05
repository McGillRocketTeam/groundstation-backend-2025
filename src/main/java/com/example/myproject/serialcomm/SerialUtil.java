package com.example.myproject.serialcomm;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import com.google.common.base.Stopwatch;
import org.yamcs.AbstractYamcsService;
import org.yamcs.InitException;
import org.yamcs.YConfiguration;
import org.yamcs.logging.Log;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//todo look into using serialPort.getPortDescription() for logging since it gives the name of the device connected, verify this works on mac

public class SerialUtil extends AbstractYamcsService implements Runnable{
    private Log log;
    private ScheduledExecutorService executorService;
    private final Set<String> existingSerialPorts = new HashSet<>();
    private int checkInactivePortsAgainCounter = 0;

    @Override
    public void init(String yamcsInstance, String serviceName, YConfiguration config) throws InitException {
        super.init(yamcsInstance, serviceName, config);

        log = new Log(getClass(), yamcsInstance);
        log.setContext("Serial Utilities");
    }

    @Override
    protected void doStart() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this, 0, 20, TimeUnit.SECONDS);
        notifyStarted();
    }

    @Override
    protected void doStop() {
        if(executorService != null){
            executorService.shutdown();
        }
        notifyStopped();
    }

    @Override
    public void run() {

        if(SerialDataLink.activePorts.size() == SerialDataLink.uniqueIdentifierToLink.size()){
            return;
        }

        checkInactivePortsAgainCounter++;

        for(SerialPort serialPort : SerialPort.getCommPorts()){
            if(!existingSerialPorts.contains(serialPort.getSystemPortName()) || checkInactivePortsAgainCounter > 5){
                existingSerialPorts.add(serialPort.getSystemPortName());

                if(SerialDataLink.activePorts.contains(serialPort.getSystemPortName())){
                    continue;
                }

                String uniqueIdentifier = checkForValidPing(serialPort);

                if(uniqueIdentifier == null){
                    continue;
                }

                SerialDataLink serialDataLink = SerialDataLink.uniqueIdentifierToLink.get(uniqueIdentifier);

                if(serialDataLink == null){
                    log.error("Successfully pinged port " + serialPort.getSystemPortName()
                            + " but no serial data link exists with its unique identifier of: " + uniqueIdentifier);
                    continue;
                }

                serialDataLink.connectToPort(serialPort);
            }
        }

        if(checkInactivePortsAgainCounter > 5){
            checkInactivePortsAgainCounter = 0;
        }
    }


    /**
     * The general format of a ping for a ground station connected to an FC is:
     * ping_ack:spread_factor,bandwidth,radio_frequency,coding_rate
     * <p>
     * The general format of a ping for a control box is:
     * ping_ack:control_box,,,
     * <p>
     * This method pings the given serial port, and waits 6 seconds for a response.
     * If the response received from the serial port is a valid ping acknowledgement,
     * the unique identifier of the device connected to the serial port is extracted from
     * the acknowledgement and returned by the method. Otherwise, null is returned.
     * <p>
     * Note that For ground stations connected to FCs, the unique identifier is their radio frequency
     * For the control box, the unique identifier is the string "control_box"
     * @param serialPort serial port to ping
     * @return unique identifier of device connected to port; null if device is invalid
     */
    private String checkForValidPing(SerialPort serialPort){
        log.info("Checking for valid ping on port " + serialPort.getSystemPortName());

        if(!serialPort.openPort()){
            log.info("Failed to open new serial port " + serialPort.getSystemPortName());
            return null;
        }

        serialPort.setComPortParameters(9600,8,1,0);

        final String[] uniqueIdentifier = {null};

        serialPort.addDataListener(new SerialPortMessageListener() {
            @Override
            public byte[] getMessageDelimiter() {
                //TODO verify if packets end with \n or \r or \n\r
                return new byte[]{'\n'};
            }

            @Override
            public boolean delimiterIndicatesEndOfMessage() {
                return true;
            }

            //will call serialEvent method when port disconnected or data is received
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
            }

            @Override
            public void serialEvent(SerialPortEvent serialPortEvent) {
                String receivedMessage = new String(serialPortEvent.getReceivedData(), StandardCharsets.UTF_8);

                if(receivedMessage.startsWith("ping_ack")){
                    String[] pingParams = receivedMessage.trim().split(":")[1].split(",");

                    if(pingParams[0].equals("control_box")){
                        uniqueIdentifier[0] = pingParams[0];
                    } else if(pingParams[2].matches("^\\d+.\\d+$")){
                        uniqueIdentifier[0] = pingParams[2];
                    }
                }
            }
        });

        try {
            Thread.sleep(500); //unsure if this is necessary, test without
            byte[] pingMessage = "ping".getBytes(StandardCharsets.UTF_8);
            serialPort.writeBytes(pingMessage, pingMessage.length);

            log.info("Port " + serialPort.getSystemPortName() + " pinged");

            Thread.sleep(500); //unsure if this is necessary, test without
        } catch (InterruptedException e) {
            log.error("Failed to ping port " + serialPort.getSystemPortName());
            return null;
        }


        Stopwatch stopwatch = Stopwatch.createStarted();

        while(stopwatch.elapsed(TimeUnit.SECONDS) < 5 && uniqueIdentifier[0] == null);

        serialPort.removeDataListener();
        serialPort.closePort();

        return uniqueIdentifier[0];
    }
}
