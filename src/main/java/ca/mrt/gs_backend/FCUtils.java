package ca.mrt.gs_backend;

import org.yamcs.commanding.PreparedCommand;
import org.yamcs.mdb.Mdb;
import org.yamcs.mdb.MdbFactory;
import org.yamcs.xtce.MetaCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FCUtils {
    private static final Map<String, List<MetaCommand>> fcStrToMetaCmd = new HashMap<>();

    private static void getFCStrToMetaCmd() {
        if (fcStrToMetaCmd.isEmpty()) {
            Mdb mdb = MdbFactory.getInstance("gs_backend");

            var metaCommands = mdb.getMetaCommands();
            metaCommands.stream().filter((cmd) -> cmd.getQualifiedName().contains("FlightComputer")).forEach((fcCmd) -> {
                String cmdStr = fcCmd.getShortDescription().split(" ")[0];
                List<MetaCommand> cmds = fcStrToMetaCmd.getOrDefault(cmdStr, new ArrayList<>());
                cmds.add(fcCmd);
                fcStrToMetaCmd.put(cmdStr, cmds);
            });
        }
    }

    public static List<MetaCommand> getMetaCmds(String fcStr){
        getFCStrToMetaCmd();
        return fcStrToMetaCmd.get(fcStr);
    }

    public static String getCmdStrFromCmd(PreparedCommand command) {
        StringBuilder cmd = new StringBuilder(command.getMetaCommand().getShortDescription().split(" ")[0]);

        if(command.getArgAssignment().values().size() == 1){
            for(var arg : command.getArgAssignment().values()){
                cmd.append(arg.getEngValue());
            }        }
        for(var arg : command.getArgAssignment().values()){
            cmd.append(",").append(arg.getEngValue());
        }
        return cmd.toString().replace('_', ' ');
    }

    public static byte[] getDelimiter(){
        return new byte[]{'<', 'L', 'E', 'O', '?', '>'};
    }


    public static String getAckStrFromCmd(PreparedCommand command) {
        return command.getCmdName().equals("ping") ? "ping_ack" : command.getMetaCommand().getShortDescription().split(" ")[1];
    }
}
