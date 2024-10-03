package ca.mrt.gs_backend.serialcomm;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import org.yamcs.ConfigurationException;
import org.yamcs.Spec;
import org.yamcs.TmPacket;
import org.yamcs.YConfiguration;
import org.yamcs.tctm.AbstractTmDataLink;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SerialDataLink extends AbstractTmDataLink implements Runnable{

    protected static Map<String, SerialDataLink> uniqueIdentifierToLink = new HashMap<>();
    protected static Set<String> activePorts = new HashSet<>();

    //todo maybe change this to an Optional
    private SerialPort currConnectedPort;
    private long timeOfLastPacket = System.currentTimeMillis();

    /**
     * For ground stations connected to FCs, the unique identifier is their radio frequency
     * For the control box, the unique identifier is the string "control_box"
     */
    private String uniqueIdentifier;
    private final Queue<TmPacket> packetQueue = new ConcurrentLinkedQueue<>();

    protected void connectToPort(SerialPort serialPort){
        if(currConnectedPort != null){
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

                TmPacket tmPacket = new TmPacket(timeService.getMissionTime(), serialPortEvent.getReceivedData());

                packetQueue.add(packetPreprocessor.process(tmPacket));
            }
        });

        activePorts.add(serialPort.getSystemPortName());
    }

    private void disconnectFromCurrPort(){
        if(currConnectedPort == null){
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
            thread.setName(getClass().getSimpleName() + "-" + linkName + " " + uniqueIdentifier);
            thread.start();
        }
        notifyStarted();
    }

    @Override
    protected void doStop() {
        if(currConnectedPort != null){
            disconnectFromCurrPort();
        }
        notifyStopped();
    }


    @Override
    public void run() {
        while(isRunningAndEnabled()){
            if(!packetQueue.isEmpty()){
                TmPacket tmPacket = packetQueue.poll();
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
    public Spec getSpec() {
        var spec = getDefaultSpec();
        spec.addOption("unique_identifier", Spec.OptionType.STRING);
        return spec;
    }

    @Override
    public void init(String instance, String name, YConfiguration config) throws ConfigurationException {
        super.init(instance, name, config);
        uniqueIdentifier = config.getString("unique_identifier");
        uniqueIdentifierToLink.put(uniqueIdentifier, this);

        if(!uniqueIdentifier.equals("control_box") && !uniqueIdentifier.matches("^\\d+.\\d+$")){
            throw new ConfigurationException("The 'unique_identifier' config must either be 'control_box' or a decimal number");
        }
    }

    @Override
    public void doDisable() {
        if (currConnectedPort != null) {
            disconnectFromCurrPort();
        }
    }

    @Override
    public String getDetailedStatus() {
        if (isDisabled()) {
            return "DISABLED";
        } else if(currConnectedPort != null){
            return "OK, receiving on port " + currConnectedPort.getSystemPortName();
        } else {
            return "UNAVAILABLE, not connected to any port";
        }
    }
}