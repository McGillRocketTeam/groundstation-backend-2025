package ca.mrt.gs_backend.networkcomm;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.primitives.Bytes;
import org.yamcs.*;
import org.yamcs.Spec.OptionType;
import org.yamcs.cmdhistory.CommandHistoryPublisher;
import org.yamcs.commanding.PreparedCommand;
import org.yamcs.protobuf.Commanding;
import org.yamcs.tctm.AbstractTcTmParamLink;

public abstract class TcpTcTmDataLinkServer extends AbstractTcTmParamLink implements Runnable {
    protected ServerSocket serverSocket;
    protected Socket clientSocket;

    protected int port;
    private OutputStream outputStream;
    private InputStream inputStream;
    private final ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
    private final Map<String, Commanding.CommandId> ackStrToMostRecentCmdId = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static CommandHistoryPublisher ackPublisher;



    @Override
    public Spec getSpec() {
        var spec = getDefaultSpec();
        spec.addOption("port", OptionType.INTEGER).withRequired(true);
        return spec;
    }

    @Override
    public void init(String instance, String name, YConfiguration config) throws ConfigurationException {
        super.init(instance, name, config);
        port = config.getInt("port");
    }

    protected synchronized void waitForClient() throws IOException {
        if (isSocketOpen()) return;

        if (serverSocket == null || serverSocket.isClosed()) {
            serverSocket = new ServerSocket(port);
            log.info("TCP Server listening on port {}", port);
        }

        clientSocket = serverSocket.accept();
        log.info("Client connected from {}", clientSocket.getRemoteSocketAddress());

        outputStream = clientSocket.getOutputStream();
        inputStream = clientSocket.getInputStream();
    }

    protected synchronized boolean isSocketOpen() {
        return clientSocket != null && !clientSocket.isClosed();
    }

    protected synchronized void sendBuffer(byte[] data) throws IOException {
        if (outputStream == null) {
            throw new IOException("No client connected");
        }
        outputStream.write(data);
    }

    protected synchronized void closeClientSocket() {
        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException e) {
            }
            clientSocket = null;
            outputStream = null;
            inputStream = null;
        }
    }

    @Override
    public void run() {
        while (isRunningAndEnabled()) {
            getNextPacket();
        }
    }

    protected abstract byte[] getDelimiter();
    protected abstract String getAckStrFromCmd(PreparedCommand command);

    protected abstract String getCmdStrFromCmd(PreparedCommand command);

    private boolean processAck(String ackText) {
        String ackName = ackText.trim();
        if (!ackStrToMostRecentCmdId.containsKey(ackName)) {
            return ackText.toUpperCase().contains("ACK");
        }

        var cmdId = ackStrToMostRecentCmdId.get(ackName);
        getAckPublisher().publishAck(cmdId, "custom ack", timeService.getMissionTime(), CommandHistoryPublisher.AckStatus.OK);
        ackStrToMostRecentCmdId.remove(ackName);
        return true;
    }



    public void getNextPacket() {
        byte[] delimiter = getDelimiter(); // e.g., "\n".getBytes()

        while (isRunningAndEnabled()) {
            try {
                waitForClient();

                byte[] bytes = inputStream.readNBytes(256);
                if (bytes.length == 0) {
                    continue;
                }
                dataIn(1, bytes.length);
                log.info("Received " + bytes.length + " bytes");

                byteBuffer.write(bytes); // append to buffer
                byte[] bufferBytes = byteBuffer.toByteArray();

                int idx;
                while ((idx = Bytes.indexOf(bufferBytes, delimiter)) != -1) {
                    // Found a full packet ending at idx
                    byte[] packetData = new byte[idx];
                    System.arraycopy(bufferBytes, 0, packetData, 0, packetData.length - 1);

                    if(bufferBytes.length - packetData.length - delimiter.length > 0){
                        byte[] newBuffer = new byte[bufferBytes.length - idx - delimiter.length];
                        System.arraycopy(bufferBytes, idx + delimiter.length, newBuffer, 0, newBuffer.length - 1);
                        bufferBytes = newBuffer;
                    }

                    String dataStr = new String(packetData);
                    if (processAck(dataStr)) { //incoming message is an ack
                        log.info("Received ack: " + dataStr);
                        return;
                    }

                    TmPacket pkt = new TmPacket(timeService.getMissionTime(), packetData);
                    pkt.setEarthReceptionTime(timeService.getHresMissionTime());
                    TmPacket processed = packetPreprocessor.process(pkt);
                    if (processed != null) {
                        processPacket(processed);
                    }
                }

                // Retain only unprocessed data in the buffer
                ByteArrayOutputStream newBuf = new ByteArrayOutputStream();
                if (0 < bufferBytes.length) {
                    newBuf.write(bufferBytes, 0, bufferBytes.length);
                }
                byteBuffer.reset();
                byteBuffer.writeBytes(newBuf.toByteArray());

            } catch (EOFException e) {
                log.warn("Client disconnected");
                closeClientSocket();
            } catch (IOException e) {
                log.warn("I/O error with client: {}", e.toString());
                closeClientSocket();
                try {
                    Thread.sleep(1000); // wait before retry
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

    }

    @Override
    public boolean sendCommand(PreparedCommand pc) {
        if(clientSocket.isClosed()){
            log.warn("Attempting to send tcp commands while not connected to this device");
        }
        String cmdStr = getCmdStrFromCmd(pc);
        String ackStr = getAckStrFromCmd(pc);

        try {
            sendBuffer((cmdStr + '\n') .getBytes());
            dataOutCount.getAndIncrement();
            ackCommand(pc.getCommandId());
            log.info("Sent " + cmdStr + " to " + clientSocket.getRemoteSocketAddress());

            ackStrToMostRecentCmdId.put(ackStr, pc.getCommandId());
            scheduler.schedule(() -> {
                if (ackStrToMostRecentCmdId.containsKey(ackStr)) {
                    log.warn("Didn't receive ack for cmd: " + cmdStr);

                    var cmdId = ackStrToMostRecentCmdId.get(ackStr);
                    getAckPublisher().publishAck(cmdId, "custom ack", timeService.getMissionTime(), CommandHistoryPublisher.AckStatus.TIMEOUT);
                    ackStrToMostRecentCmdId.remove(ackStr);
                }
            }, 15, TimeUnit.SECONDS);

            return true;
        } catch (IOException e) {
            String reason = "Error sending to client: " + e;
            log.warn(reason);
            failedCommand(pc.getCommandId(), reason);
            return true;
        }
    }

    private static CommandHistoryPublisher getAckPublisher() {
        if (ackPublisher == null) {
            ackPublisher = YamcsServer.getServer().getProcessor("gs_backend", "realtime").getCommandHistoryPublisher();
        }
        return ackPublisher;
    }


    @Override
    public void doStart() {
        if (!isDisabled()) {
            Thread thread = new Thread(this);
            thread.setName(getClass().getSimpleName() + "-" + linkName);
            thread.start();
        }
        notifyStarted();
    }

    @Override
    public void doStop() {
        closeClientSocket();
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
            }
        }
        notifyStopped();
    }

    @Override
    public void doDisable() {
        closeClientSocket();
    }

    @Override
    public void doEnable() {
        Thread thread = new Thread(this);
        thread.setName(getClass().getSimpleName() + "-" + linkName);
        thread.start();
    }

    @Override
    public String getDetailedStatus() {
        if (isDisabled()) {
            return String.format("DISABLED (listening on port %d)", port);
        }
        if (isSocketOpen()) {
            return String.format("OK, client connected from %s", clientSocket.getRemoteSocketAddress());
        } else {
            return String.format("Listening on port %d (no client connected)", port);
        }
    }

    @Override
    protected Status connectionStatus() {
        return isSocketOpen() ? Status.OK : Status.UNAVAIL;
    }
}
