package ca.mrt.gs_backend.serialcomm;

import org.yamcs.commanding.PreparedCommand;

public class ControlBoxLink extends SerialDataLink{
    @Override
    protected String getAckStrFromCmd(PreparedCommand command) {
        return command.getCmdName().equals("ping") ? "ping_ack" : command.getCmdName();
    }

    @Override
    protected String getCmdStrFromCmd(PreparedCommand command) {
        return null;
    }
}
