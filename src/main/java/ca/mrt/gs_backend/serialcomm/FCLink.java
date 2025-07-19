package ca.mrt.gs_backend.serialcomm;

import ca.mrt.gs_backend.FCUtils;
import org.yamcs.Spec;
import org.yamcs.commanding.PreparedCommand;
import org.yamcs.mdb.Mdb;
import org.yamcs.mdb.MdbFactory;
import org.yamcs.xtce.MetaCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FCLink extends SerialDataLink{

    @Override
    public Spec getSpec() {
        var spec = getDefaultSpec();
        spec.addOption("frequency", Spec.OptionType.STRING);
        return spec;
    }

    @Override
    protected String getAckStrFromCmd(PreparedCommand command) {
        return FCUtils.getAckStrFromCmd(command);
    }

    @Override
    protected String getCmdStrFromCmd(PreparedCommand command) {
        return FCUtils.getCmdStrFromCmd(command);
    }

    @Override
    protected byte[] getDelimiter(){
        return FCUtils.getDelimiter();
    }
}
