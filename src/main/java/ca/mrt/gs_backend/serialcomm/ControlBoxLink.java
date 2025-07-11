package ca.mrt.gs_backend.serialcomm;

import ca.mrt.gs_backend.labjack.LabJackDataLink;
import org.yamcs.YamcsServer;
import org.yamcs.commanding.CommandReleaser;
import org.yamcs.commanding.CommandingManager;
import org.yamcs.commanding.PreparedCommand;
import org.yamcs.xtce.MetaCommand;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ControlBoxLink extends SerialDataLink {
    private ScheduledExecutorService actuatorScheduler = Executors.newSingleThreadScheduledExecutor();
    private Map<String, List<MetaCommand>> fcStrToCmdMap;
    private CommandingManager cmdManager;
    private CommandReleaser cmdReleaser;
    private static int NUM_ACTUATOR_ALTERNATIONS = 4000;

    public ControlBoxLink() {
        addListener(newData -> {
            if (fcStrToCmdMap == null) {
                fcStrToCmdMap = FCLink.getReverseMapping();
            }
            if(cmdManager == null){
                var processor = YamcsServer.getServer().getProcessor("gs_backend", "realtime");
                cmdManager = processor.getCommandingManager();
                cmdReleaser = processor.getCommandReleaser();
            }

            switch (newData) {
                case "VENT PUSHED":
                    sendFCCmd("p0");
                    break;
                case "VENT RELEASED":
                    sendFCCmd("p1");
                    break;
                case "UFD PUSHED":
                    sendFCCmd("p4");
                    break;
                case "UFD RELEASED":
                    sendFCCmd("p5");
                    break;
                case "KEY PUSHED":
                    sendFCCmd("p2");
                    break;
                case "KEY RELEASED":
                    sendFCCmd("p3");
                    break;
                case "RUN PUSHED":
                    sendFCCmd("pl");
                    break;
                case "RUN RELEASED":
                    break;
                case "EMERGENCY STOP PUSHED":
                    sendFCCmd("pe");
                    break;
                case "EMERGENCY STOP RELEASED":
                    sendFCCmd("pc");
                    break;
                case "PANEL FILL PUSHED":
                    LabJackDataLink.getInstance().writeDigitalPin(2, 1);
                    break;
                case "PANEL FILL RELEASED":
                    LabJackDataLink.getInstance().writeDigitalPin(2, 0);
                    break;
                case "PANEL DUMP PUSHED":
                    LabJackDataLink.getInstance().writeDigitalPin(3, 1);
                    break;
                case "PANEL DUMP RELEASED":
                    LabJackDataLink.getInstance().writeDigitalPin(3, 0);
                    break;
                case "ACTUATOR POWER PUSHED":
                    LabJackDataLink.getInstance().writeDigitalPin(0, 1);
                    break;
                case "ACTUATOR POWER RELEASED":
                    LabJackDataLink.getInstance().writeDigitalPin(0, 0);
                    break;
                case "ACTUATOR POLARITY PUSHED":
                    LabJackDataLink.getInstance().writeDigitalPin(1, 1);
                    break;
                case "ACTUATOR POLARITY RELEASED":
                    LabJackDataLink.getInstance().writeDigitalPin(1, 0);
                    break;
                case "IGN PLUS PUSHED":
                    LabJackDataLink.getInstance().writeDigitalPin(6, 1);
                    break;
                case "IGN PLUS RELEASED":
                    break;
                case "IGN MINUS PUSHED":
                    LabJackDataLink.getInstance().writeDigitalPin(7, 1);
                    break;
                case "IGN MINUS RELEASED":
                    break;
                case "ACTUATOR B POWER PUSHED":
                    LabJackDataLink.getInstance().writeDigitalPin(20, 1);
                    break;
                case "ACTUATOR B POWER RELEASED":
                    break;
                case "ACTUATOR B POLARITY PUSHED":
                    LabJackDataLink.getInstance().writeDigitalPin(22, 1);
                    break;
                case "ACTUATOR B POLARITY RELEASED":
                    break;

            }
        });
    }

    private void actuate(int digitalPin) {

        actuatorScheduler.scheduleAtFixedRate(new Runnable() {
                                                  int itrCnt = 0;
                                                  int voltage = 0;

                                                  @Override
                                                  public void run() {
                                                      voltage = voltage == 0 ? 1 : 0;

                                                      LabJackDataLink.getInstance().writeDigitalPin(digitalPin, voltage);
                                                      if (itrCnt++ >= NUM_ACTUATOR_ALTERNATIONS) {
                                                          actuatorScheduler.shutdown();
                                                      }
                                                  }
                                              }
                , 0, 250000, TimeUnit.NANOSECONDS);
    }

    private void sendFCCmd(String fcCmd){
        fcStrToCmdMap.get(fcCmd).forEach((metaCommand -> {
            var prepCmd = cmdManager.buildRawCommand(metaCommand, new byte[]{}, "yamcs-internal", 0, YamcsServer.getServer().getSecurityStore().getSystemUser());
            cmdReleaser.releaseCommand(prepCmd);
        }));

    }

    @Override
    protected String getAckStrFromCmd(PreparedCommand command) {
        return command.getCmdName().equals("ping") ? "ping_ack" : command.getCmdName();
    }

    @Override
    protected String getCmdStrFromCmd(PreparedCommand command) {
        return null;
    }

    @Override
    protected byte[] getDelimiter() {
        return new byte[]{'\r', '\n'};
    }
}
