package ca.mrt.serial_terminal;

import ca.mrt.gs_backend.serialcomm.SerialDataLink;
import ca.mrt.gs_backend.serialcomm.SerialListener;
import ca.mrt.serial_terminal.api.AbstractSerialTerminalAPI;
import ca.mrt.serial_terminal.api.SerialTerminalData;
import ca.mrt.serial_terminal.api.SubscribeSerialTerminalRequest;
import org.yamcs.api.Observer;
import org.yamcs.http.Context;


public class SerialTerminalAPI extends AbstractSerialTerminalAPI<Context> {
    @Override
    public void subsribeSerialTerminal(Context ctx, SubscribeSerialTerminalRequest request, Observer<SerialTerminalData> observer) {

        SerialListener listener = newData -> {
            SerialTerminalData serialTerminalData = SerialTerminalData.newBuilder()
                    .setMessages(newData)
                    .build();
            observer.next(serialTerminalData);
        };

        SerialDataLink.addListener(listener);

        observer.setCancelHandler(() -> SerialDataLink.removeListener(listener));
    }
}
