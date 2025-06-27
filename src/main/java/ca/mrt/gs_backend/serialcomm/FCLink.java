package ca.mrt.gs_backend.serialcomm;

import org.yamcs.Spec;
import org.yamcs.YamcsServer;
import org.yamcs.commanding.PreparedCommand;
import org.yamcs.mdb.Mdb;
import org.yamcs.mdb.MdbFactory;
import org.yamcs.xtce.MetaCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FCLink extends SerialDataLink{
    private static Map<String, List<MetaCommand>> fcStrToMetaCmd = new HashMap<>();

    public static Map<String, List<MetaCommand>> getReverseMapping() {
        if(!fcStrToMetaCmd.isEmpty()){
            return fcStrToMetaCmd;
        }

        Mdb mdb = MdbFactory.getInstance("gs_backend");

        var metaCommands = mdb.getMetaCommands();
        metaCommands.stream().filter((cmd) -> cmd.getQualifiedName().contains("FlightComputer")).forEach((fcCmd) -> {
            String cmdStr = fcCmd.getShortDescription().split(" ")[0];
            List<MetaCommand> cmds = fcStrToMetaCmd.getOrDefault(cmdStr, new ArrayList<>());
            cmds.add(fcCmd);
            fcStrToMetaCmd.put(cmdStr, cmds);
        });
        return fcStrToMetaCmd;
    }

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
        StringBuilder cmd = new StringBuilder(command.getMetaCommand().getShortDescription().split(" ")[0]);

        for(var arg : command.getArgAssignment().entrySet()){
            cmd.append(",").append(arg.getValue().getEngValue());
        }
        return cmd.toString();
    }

    @Override
    protected byte[] getDelimiter(){
        return new byte[]{'<', 'L', 'E', 'O', '?', '>'};
    }
}
