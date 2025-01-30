package ca.mrt.gs_backend.serialcomm;

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
import java.util.concurrent.*;

//todo look into using serialPort.getPortDescription() for logging since it gives the name of the device connected, verify this works on mac

/**
 * @author Jake
 * This class is a singleton service whose sole purpose is to determine which comports (USB) have an MRT device
 * connected that have a corresponding SerialDataLink instance with that device's unique identifier. (matchmaker)
 * <p>
 * This class keeps track of all existing comports {@link #existingSerialPorts} which have been tested for device connectivity previously but
 * could not be mapped to a SerialDataLink instance.
 * <p>
 * Every 20s it checks for new comports which have not been pinged yet. Every 100s it re-pings all previously pinged
 * comports to check if any new device was connected. Whenever it 'checks' a comport, it pings and awaits a response.
 * If it receives a response of the correct format, its able to identify if the comport has an FC or a control box connected
 * and if it is an FC, the radio frequency that the FC is on. From this, it will attempt to find a {@link SerialDataLink} instance
 * which has a matching unique identifier. Once the comport is given to the {@link SerialDataLink} instance, it is added to a set
 * {@link SerialDataLink#activePorts} of comports to no longer ping and should not be written to or read from by this class.
 */
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
                log.info("Connecting to " + uniqueIdentifier);
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
            ExecutorService executor = Executors.newSingleThreadExecutor();

            Callable<String> task = () -> {
                serialPort.writeBytes(pingMessage, pingMessage.length);
                return "Task Completed";
            };

            Future<String> future = executor.submit(task);

            try {
                future.get(100, TimeUnit.MILLISECONDS); // Timeout of 2 seconds
            } catch (TimeoutException e) {
                log.warn("Timeout: writing ping took too long");
                future.cancel(true); // Cancel the task if it exceeds the timeout
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } finally {
                executor.shutdown();
            }

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

        if(uniqueIdentifier[0] == null){
            return null;
        }

        return uniqueIdentifier[0].split("\\.")[0];
    }
}
