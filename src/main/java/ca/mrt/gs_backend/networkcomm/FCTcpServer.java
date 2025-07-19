package ca.mrt.gs_backend.networkcomm;

import ca.mrt.gs_backend.FCUtils;
import org.yamcs.commanding.PreparedCommand;

public class FCTcpServer extends TcpTcTmDataLinkServer{
    @Override
    protected byte[] getDelimiter() {
        return FCUtils.getDelimiter();
    }

    @Override
    protected String getAckStrFromCmd(PreparedCommand command) {
        return FCUtils.getAckStrFromCmd(command);
    }

    @Override
    protected String getCmdStrFromCmd(PreparedCommand command) {
        return FCUtils.getCmdStrFromCmd(command);
    }


}
