package ca.mrt.gs_backend.serialcomm;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import org.yamcs.ConfigurationException;
import org.yamcs.TmPacket;
import org.yamcs.YConfiguration;
import org.yamcs.YamcsServer;
import org.yamcs.client.processor.ProcessorClient;
import org.yamcs.cmdhistory.CommandHistoryPublisher;
import org.yamcs.commanding.CommandingManager;
import org.yamcs.commanding.PreparedCommand;
import org.yamcs.protobuf.Commanding;
import org.yamcs.tctm.AbstractTcTmParamLink;
import org.yamcs.tctm.TmSink;
import org.yamcs.yarch.DataType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Jake
 * This class manages a connection with a serial (USB) device.
 * In the context of MRT operations (as of now), this device could be a groundstation radio
 * connected to a flight computer on a unique frequency OR a control box.
 * <p>
 * Since we must have the ability to connect to multiple FCs at the same time, and we will
 * never have more than one FC on the same frequency, we use an option called the
 * 'unique identifier' to inform the data link which device it will be connecting to.
 * If the data link is supposed to connect to an FC, the unique identifier is the radio frequency
 * which that FC is on (e.g. 433).
 * If the data link is suppossed to connect to a control box, the unique identifier will be 'control_box'.
 * <p>
 * The way in which these SerialDataLink instances are connected to the correct device on the correct serial port
 * is controlled by the {@link SerialUtil} class.
 */
public abstract class SerialDataLink extends AbstractTcTmParamLink implements Runnable {

    protected static Map<String, SerialDataLink> uniqueIdentifierToLink = new HashMap<>();
    protected static Set<String> activePorts = new HashSet<>();
    private static CommandHistoryPublisher ackPublisher;
    private final Set<ca.mrt.gs_backend.serialcomm.Listener> listeners = new HashSet<>();

    //todo maybe change this to an Optional
    private SerialPort currConnectedPort;
    private long timeOfLastPacket = System.currentTimeMillis();

    /**
     * For ground stations connected to FCs, the unique identifier is their radio frequency
     * For the control box, the unique identifier is the string "control_box"
     */
    private String uniqueIdentifier;
    private final Queue<TmPacket> packetQueue = new ConcurrentLinkedQueue<>();
    private final Map<String, Commanding.CommandId> ackStrToMostRecentCmdId = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private Integer isGSRadio() {
        if (uniqueIdentifier.startsWith("gs_radio_")) {
            String digitsString = uniqueIdentifier.substring("gs_radio_".length());
            return Integer.parseInt(digitsString);
        } else {
            return null;
        }
    }

    public void addListener(ca.mrt.gs_backend.serialcomm.Listener listener) {
        listeners.add(listener);
    }

    public boolean removeListener(ca.mrt.gs_backend.serialcomm.Listener listener) {
        return listeners.remove(listener);
    }


    protected void connectToPort(SerialPort serialPort) {
        if (isCurrentlyConnected()) {
            log.error("Cannot connect to new serial port before disconnecting from existing port: "
                    + currConnectedPort.getSystemPortName());
            return;
        }

        if (!serialPort.openPort()) {
            log.error("Failed to open new serial port: " + serialPort.getSystemPortName());
            return;
        }

        serialPort.setComPortParameters(9600, 8, 1, 0);

        serialPort.addDataListener(new SerialPortMessageListener() {
            @Override
            public byte[] getMessageDelimiter() {
                //TODO verify if packets end with \n or \r or \n\r
                return getDelimiter();
                //<LEO?>
            }

            @Override
            public boolean delimiterIndicatesEndOfMessage() {
                return true;
            }

            //will call serialEvent method when port disconnected or data is received
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_PORT_DISCONNECTED | SerialPort.LISTENING_EVENT_DATA_RECEIVED;
            }

            @Override
            public void serialEvent(SerialPortEvent serialPortEvent) {

                if (serialPortEvent.getEventType() == SerialPort.LISTENING_EVENT_PORT_DISCONNECTED) {
                    log.warn("Serial port " + currConnectedPort.getSystemPortName() + " disconnected");
                    disconnectFromCurrPort();
                }
                timeOfLastPacket = System.currentTimeMillis();
                var temp = (new String(serialPortEvent.getReceivedData()));
                log.info(temp);

                String dataStr = temp.replace(new String(getDelimiter()), "");
                dataIn(1, dataStr.length());

                for (ca.mrt.gs_backend.serialcomm.Listener listener : listeners) {
                    listener.notifyUpdate(dataStr);
                }

                if (uniqueIdentifier.equals("control_box")) {
                    return;
                }

                if (uniqueIdentifier.equals("gs_radio")) {
                    return;
                }

                if (processAck(dataStr)) { //incoming message is an ack
                    log.info("Received ack: " + dataStr);
                    return;
                }

                byte[] trimmed_array = new byte[94];
                System.arraycopy(serialPortEvent.getReceivedData(), 0, trimmed_array, 0, trimmed_array.length);


                TmPacket tmPacket = new TmPacket(getCurrentTime(), trimmed_array);
                packetQueue.add(packetPreprocessor.process(tmPacket));
            }
        });

        activePorts.add(serialPort.getSystemPortName());
        currConnectedPort = serialPort;

        log.info("Writing \"radio init\"");
        byte[] dataToWrite = ("radio init" + "\r\n").getBytes(StandardCharsets.UTF_8);
        try {
            currConnectedPort.getOutputStream().write(dataToWrite);
        } catch (IOException e) {
            log.error("Failed to write radio init command");
        }
    }

    private boolean processGSRadioAck(String ackText) {
        String prefix = "ack_rGSRadio";
        // This is not a GSRadio Ack
        if (!ackText.startsWith(prefix)) {
            log.info("returning");
            return false;
        } else {
            ackText = ackText.substring(prefix.length());
        }

        byte[] bytes = ackText.getBytes();

        SerialDataLink link = uniqueIdentifierToLink.get("gs_radio_903");
        if (link != null) {
            TmPacket tmPacket = new TmPacket(getCurrentTime(), bytes);
            link.packetQueue.add(link.packetPreprocessor.process(tmPacket));
        } else {
            log.error("GSRadio Link was null");
            var set = uniqueIdentifierToLink.keySet();
            log.error(String.join(" ", set));
        }


        String ackName = "ack_r";
        var cmdId = ackStrToMostRecentCmdId.get(ackName);
        if (cmdId != null) {
            getAckPublisher().publishAck(cmdId, "custom ack", timeService.getMissionTime(), CommandHistoryPublisher.AckStatus.OK);
            ackStrToMostRecentCmdId.remove(ackName);
        }

        return true;
    }

    private boolean processAck(String ackText) {
        // GSRadio ACKs come with extra data that needs to be parsed
        if (ackText.contains("GSRadio")) {
            return processGSRadioAck(ackText);
        }

        String ackName = ackText.trim();
        if (!ackStrToMostRecentCmdId.containsKey(ackName)) {
            return ackText.toUpperCase().contains("ACK");
        }

        var cmdId = ackStrToMostRecentCmdId.get(ackName);
        getAckPublisher().publishAck(cmdId, "custom ack", timeService.getMissionTime(), CommandHistoryPublisher.AckStatus.OK);
        ackStrToMostRecentCmdId.remove(ackName);
        return true;
    }

    protected abstract String getAckStrFromCmd(PreparedCommand command);

    protected abstract String getCmdStrFromCmd(PreparedCommand command);

    protected abstract byte[] getDelimiter();

    private void disconnectFromCurrPort() {
        if (!isCurrentlyConnected()) {
            log.error("Cannot disconnect from port since no port is connected");
            return;
        }

        currConnectedPort.removeDataListener();
        if (!currConnectedPort.closePort()) {
            log.error("Cannot close port: " + currConnectedPort.getSystemPortName());
        }
        activePorts.remove(currConnectedPort.getSystemPortName());
        currConnectedPort = null;
    }

    @Override
    protected void doStart() {
        if (!isDisabled()) {
            Thread thread = new Thread(this);
            thread.setName(getClass().getSimpleName() + "-" + linkName + "-" + uniqueIdentifier);
            thread.start();
        }
        notifyStarted();
    }

    @Override
    protected void doStop() {
        if (isCurrentlyConnected()) {
            disconnectFromCurrPort();
        }
        notifyStopped();
    }


    @Override
    public void run() {
        while (isRunningAndEnabled()) {
            if (!packetQueue.isEmpty()) {
                TmPacket tmPacket = packetQueue.poll();
                processPacket(tmPacket);
            }
        }
    }

    //TODO maybe change this to whether or not a serial connection is currently active
    @Override
    protected Status connectionStatus() {
        return System.currentTimeMillis() - timeOfLastPacket < 5000 ? Status.OK : Status.UNAVAIL;
    }

    @Override
    public void init(String instance, String name, YConfiguration config) throws ConfigurationException {
        super.init(instance, name, config);
        if (config.containsKey("frequency")) {
            log.info("SETTING UP: "+config.getString("frequency"));
            uniqueIdentifier = config.getString("frequency");
        } else {
            uniqueIdentifier = "control_box";
        }

        if (uniqueIdentifierToLink.put(uniqueIdentifier, this) != null) {
            throw new ConfigurationException("Cannot have duplicate unique identifiers (can't have 2 control boxes, 2 FCs with same frequency, etc.)");
        }

        if (!uniqueIdentifier.equals("control_box") && !uniqueIdentifier.contains("gs_radio") && !uniqueIdentifier.matches("^\\d+.\\d+$")) {
            throw new ConfigurationException("The 'unique_identifier' config must either be 'control_box' or a decimal number representing a frequency");
        }
	}

    @Override
    public void doEnable() {
        Thread thread = new Thread(this);
        thread.setName(getClass().getSimpleName() + "-" + linkName + "-" + uniqueIdentifier);
        thread.start();
    }

    @Override
    public void doDisable() {
        if (isCurrentlyConnected()) {
            disconnectFromCurrPort();
        }
    }

    @Override
    public String getDetailedStatus() {
        if (isDisabled()) {
            return "DISABLED";
        } else if (isCurrentlyConnected()) {
            return "OK," + getIdentifier() + " receiving on port " + currConnectedPort.getSystemPortName();
        } else {
            return "UNAVAILABLE, " + getIdentifier() + " not connected to any port";
        }
    }

    private String getIdentifier() {
        if (uniqueIdentifier.equals("control_box")) {
            return "Control Box";
        }
        return (uniqueIdentifier + "Hz");
    }

    public static SerialDataLink getLinkByIdentifier(String identifier) {
        return uniqueIdentifierToLink.get(identifier);
    }

    public boolean isCurrentlyConnected() {
        return currConnectedPort != null;
    }

    public boolean writePort(String text, Commanding.CommandId cmdId) {
        if (!isCurrentlyConnected()) {
            return false;
        }

        byte[] dataToWrite = (text.strip() + "\r\n").getBytes(StandardCharsets.UTF_8);
        try {
            currConnectedPort.getOutputStream().write(dataToWrite);
            log.info("Wrote \"" + text + "\" to " + currConnectedPort.getSystemPortName());
        } catch (IOException e) {
            log.error("Failed to write " + text + " to " + currConnectedPort.getSystemPortName());
            log.error(e.getMessage());
            if (cmdId != null) {
                getAckPublisher().publishAck(cmdId, CommandHistoryPublisher.AcknowledgeSent_KEY, timeService.getMissionTime(), CommandHistoryPublisher.AckStatus.NOK);
            }
            return false;
        }

        if (cmdId != null) {
            getAckPublisher().publishAck(cmdId, CommandHistoryPublisher.AcknowledgeSent_KEY, timeService.getMissionTime(), CommandHistoryPublisher.AckStatus.OK);
        }

        return true;
    }

    @Override
    public boolean sendCommand(PreparedCommand preparedCommand) {
        // GS Radio commmands are sent through the FC link
        // Don't send them here to avoid duplication
        if (uniqueIdentifier.startsWith("gs_radio_")){
            return false;
        } else if (!isCurrentlyConnected()) {
            log.warn("Attempting to send serial device commands while not connected to this device");
            return false;
        }
        String cmdStr = getCmdStrFromCmd(preparedCommand);
        String ackStr = getAckStrFromCmd(preparedCommand);

        log.info(currConnectedPort.getSystemPortName());

        if (!writePort(cmdStr, preparedCommand.getCommandId())) {
            log.error("Failed to write to port: " + cmdStr);
            return false;
        }

        ackStrToMostRecentCmdId.put(ackStr, preparedCommand.getCommandId());
        scheduler.schedule(() -> {
            if (ackStrToMostRecentCmdId.containsKey(ackStr)) {
                log.warn("Didn't receive ack for cmd: " + cmdStr);

                var cmdId = ackStrToMostRecentCmdId.get(ackStr);
                getAckPublisher().publishAck(cmdId, "custom ack", timeService.getMissionTime(), CommandHistoryPublisher.AckStatus.TIMEOUT);
                ackStrToMostRecentCmdId.remove(ackStr);
            }
        }, 15, TimeUnit.SECONDS);
        return true;

    }

    private static CommandHistoryPublisher getAckPublisher() {
        if (ackPublisher == null) {
            ackPublisher = YamcsServer.getServer().getProcessor("gs_backend", "realtime").getCommandHistoryPublisher();
        }
        return ackPublisher;
    }
}
