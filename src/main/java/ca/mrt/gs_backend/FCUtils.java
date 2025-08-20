package ca.mrt.gs_backend;

import org.yamcs.commanding.PreparedCommand;
import org.yamcs.mdb.Mdb;
import org.yamcs.mdb.MdbFactory;
import org.yamcs.xtce.MetaCommand;

import java.util.*;

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
        String[] commandComponents = command.getMetaCommand().getShortDescription().split(" ");

        boolean isGSRadioCommand = command
                .getMetaCommand().getQualifiedName().contains("GSRadio");

        StringBuilder cmd = new StringBuilder(String.join(" ", Arrays.copyOf(commandComponents, commandComponents.length - 1)));        // join the array back together with " " but don't include the last element in the array above ^

        // GS Radio commands encode their arguments different from FC commands
        if (isGSRadioCommand) {
            for(var arg : command.getArgAssignment().values()){
                cmd.append(" ").append(arg.getEngValue());
            }

            cmd.append("\n");
            System.out.println("=========== "+cmd);
        } else {
            if(command.getArgAssignment().values().size() == 1){
                for(var arg : command.getArgAssignment().values()){
                    cmd.append(arg.getEngValue());
                }
            }

            for(var arg : command.getArgAssignment().values()){
                cmd.append(",").append(arg.getEngValue());
            }
        }

        return cmd.toString().replace('_', ' ');
    }

    public static byte[] getDelimiter(){
        return new byte[]{'<', 'L', 'E', 'O', '?', '>'};
    }


    public static String getAckStrFromCmd(PreparedCommand command) {
        // HERE
        if (command.getCmdName().equals("ping")) return "ping_ack";
        if (command.getCmdName().equals("set_radio_params")) return "ACK_LORA";

        String[] commandComponents = command.getMetaCommand().getShortDescription().split(" ");
        // The last component of every command short description is the ack string
        return commandComponents[commandComponents.length - 1];
    }
}
