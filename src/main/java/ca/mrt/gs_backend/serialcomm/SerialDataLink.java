package ca.mrt.gs_backend.serialcomm;

import ca.mrt.gs_backend.Websocket_Service.WebsocketController;
import ca.mrt.gs_backend.Websocket_Service.WebsocketLinkPlugin;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import org.yamcs.ConfigurationException;
import org.yamcs.TmPacket;
import org.yamcs.YConfiguration;
import org.yamcs.YamcsServer;
import org.yamcs.cmdhistory.CommandHistoryPublisher;
import org.yamcs.commanding.PreparedCommand;
import org.yamcs.protobuf.Commanding;
import org.yamcs.tctm.AbstractTcTmParamLink;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

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
public abstract class SerialDataLink extends AbstractTcTmParamLink implements Runnable{

    protected static Map<String, SerialDataLink> uniqueIdentifierToLink = new HashMap<>();
    protected static Set<String> activePorts = new HashSet<>();

    //todo maybe change this to an Optional
    private SerialPort currConnectedPort;
    private long timeOfLastPacket = System.currentTimeMillis();
    private CommandHistoryPublisher ackPublisher;
    private WebsocketController websocketController;

    /**
     * For ground stations connected to FCs, the unique identifier is their radio frequency
     * For the control box, the unique identifier is the string "control_box"
     */
    private String uniqueIdentifier;
    private final Queue<TmPacket> packetQueue = new ConcurrentLinkedQueue<>();
    private final Map<String, Commanding.CommandId> ackStrToMostRecentCmdId = new HashMap<>();

    protected void connectToPort(SerialPort serialPort){
        if(isCurrentlyConnected()){
            log.error("Cannot connect to new serial port before disconnecting from existing port: "
                    + currConnectedPort.getSystemPortName());
            return;
        }

        if(!serialPort.openPort()){
            log.error("Failed to open new serial port: " + serialPort.getSystemPortName());
            return;
        }

        serialPort.setComPortParameters(9600,8,1,0);

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
                return SerialPort.LISTENING_EVENT_PORT_DISCONNECTED | SerialPort.LISTENING_EVENT_DATA_RECEIVED;
            }

            @Override
            public void serialEvent(SerialPortEvent serialPortEvent) {
                if(serialPortEvent.getEventType() == SerialPort.LISTENING_EVENT_PORT_DISCONNECTED){
                    log.warn("Serial port " + currConnectedPort.getSystemPortName() + " disconnected");
                    disconnectFromCurrPort();
                }
                timeOfLastPacket = System.currentTimeMillis();
                dataIn(1, serialPortEvent.getReceivedData().length);

                String dataStr = (new String(serialPortEvent.getReceivedData())).strip();
                if(processAck(dataStr)){ //incoming message is an ack
                    return;
                }

                TmPacket tmPacket = new TmPacket(getCurrentTime(), serialPortEvent.getReceivedData());

                packetQueue.add(packetPreprocessor.process(tmPacket));
            }
        });

        activePorts.add(serialPort.getSystemPortName());
        currConnectedPort = serialPort;
    }

    private boolean processAck(String ackText){
        String ackName = ackText.split(":")[0];
        if(!ackStrToMostRecentCmdId.containsKey(ackName)){
            return false;
        }

        if(ackPublisher == null){
            ackPublisher = YamcsServer.getServer().getProcessor("gs_backend", "realtime").getCommandHistoryPublisher();
        }

        ackPublisher.publishAck(ackStrToMostRecentCmdId.get(ackText), ackText, timeService.getMissionTime(), CommandHistoryPublisher.AckStatus.OK);
        return true;
    }

    protected abstract String getAckStrFromCmd(PreparedCommand command);

    private void disconnectFromCurrPort(){
        if(!isCurrentlyConnected()){
            log.error("Cannot disconnect from port since no port is connected");
            return;
        }

        currConnectedPort.removeDataListener();
        if(!currConnectedPort.closePort()){
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
        if(isCurrentlyConnected()){
            disconnectFromCurrPort();
        }
        notifyStopped();
    }


    @Override
    public void run() {
        while(isRunningAndEnabled()){
            if(!packetQueue.isEmpty()){
                TmPacket tmPacket = packetQueue.poll();
                websocketController.onParamUpdate(uniqueIdentifier, tmPacket.getPacket());
                processPacket(tmPacket);
            }
        }
    }

    //TODO maybe change this to whether or not a serial connection is currently active
    @Override
    protected Status connectionStatus() {
        return System.currentTimeMillis()-timeOfLastPacket < 5000 ? Status.OK : Status.UNAVAIL;
    }

    @Override
    public void init(String instance, String name, YConfiguration config) throws ConfigurationException {
        super.init(instance, name, config);
        if(config.containsKey("frequency")){
            uniqueIdentifier = config.getString("frequency");
        } else {
            uniqueIdentifier = "control_box";
        }


        if(uniqueIdentifierToLink.put(uniqueIdentifier, this) != null){
            throw new ConfigurationException("Cannot have duplicate unique identifiers (can't have 2 control boxes, 2 FCs with same frequency, etc.)");
        }

        if(!uniqueIdentifier.equals("control_box") && !uniqueIdentifier.matches("^\\d+.\\d+$")){
            throw new ConfigurationException("The 'unique_identifier' config must either be 'control_box' or a decimal number representing a frequency");
        }
        WebsocketLinkPlugin plugin  = YamcsServer.getServer().getPluginManager().getPlugin(WebsocketLinkPlugin.class);
        websocketController = plugin.getWebsocketController();
        websocketController.addLinkName(uniqueIdentifier);
    }
    @Override
    public void doEnable(){
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
        } else if(isCurrentlyConnected()){
            return "OK,"+ getIdentifier() +" receiving on port " + currConnectedPort.getSystemPortName();
        } else {
            return "UNAVAILABLE, "+ getIdentifier() +" not connected to any port";
        }
    }

    private String getIdentifier(){
        if(uniqueIdentifier.equals("control_box")){
            return "Control Box";
        }
        return (uniqueIdentifier + "Hz");
    }

    public boolean isCurrentlyConnected(){
        return currConnectedPort != null;
    }

    protected boolean writePort(String text){
        if(!isCurrentlyConnected()){
            return false;
        }

        byte[] dataToWrite = (text.strip() + "\r\n").getBytes(StandardCharsets.UTF_8);
        try {
            currConnectedPort.getOutputStream().write(dataToWrite);
            log.info("Wrote " + text + " to " + currConnectedPort.getSystemPortName());
        } catch (IOException e) {
            log.error("Failed to write " + text + " to " + currConnectedPort.getSystemPortName());
            log.error(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean sendCommand(PreparedCommand preparedCommand){
        if(!isCurrentlyConnected()){
            log.warn("Attempting to send serial device commands while not connected to this device");
            return false;
        }

        ackStrToMostRecentCmdId.put(getAckStrFromCmd(preparedCommand), preparedCommand.getCommandId());

        return writePort(preparedCommand.getCommandName());
    }
}