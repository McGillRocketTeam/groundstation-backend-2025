package ca.mrt.gs_backend.serialcomm;

import org.yamcs.Spec;
import org.yamcs.commanding.PreparedCommand;

public class FCLink extends SerialDataLink{
    @Override
    public Spec getSpec() {
        var spec = getDefaultSpec();
        spec.addOption("frequency", Spec.OptionType.STRING);
        return spec;
    }

    @Override
    protected String getAckStrFromCmd(PreparedCommand command) {
        return command.getCmdName().equals("ping") ? "ping_ack" : command.getMetaCommand().getShortDescription().split(" ")[1];
    }

    @Override
    protected String getCmdStrFromCmd(PreparedCommand command) {
        return command.getMetaCommand().getShortDescription().split(" ")[0];
    }
}
