package ca.mrt.serial_terminal;

import ca.mrt.gs_backend.serialcomm.SerialDataLink;
import ca.mrt.gs_backend.serialcomm.SerialListener;
import ca.mrt.serial_terminal.api.AbstractSerialTerminalAPI;
import ca.mrt.serial_terminal.api.SerialTerminalData;
import ca.mrt.serial_terminal.api.SubscribeSerialTerminalRequest;
import ca.mrt.serial_terminal.api.WriteSerialTerminalRequest;
import com.google.protobuf.Empty;
import org.yamcs.api.Observer;
import org.yamcs.http.Context;
import org.yamcs.http.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SerialTerminalAPI extends AbstractSerialTerminalAPI<Context> {
    @Override
    public void subsribeSerialTerminal(Context ctx, SubscribeSerialTerminalRequest request, Observer<SerialTerminalData> observer) {



        List<Runnable> cancelListenerList = new ArrayList<>();

        Pattern pattern = Pattern.compile("fc-(\\d+)");

        for (String link : request.getDataLinksList()) {

            Matcher matcher = pattern.matcher(link);
            if (matcher.find() || link.equals("control_box")) {
                String identifer = link.equals("control_box") ? "control_box" : matcher.group(1);
                SerialDataLink dataLink = SerialDataLink.getLinkByIdentifier(identifer);
                if (dataLink != null) {
                    SerialListener listener = newData -> {
                        SerialTerminalData serialTerminalData = SerialTerminalData.newBuilder()
                                .setMessage(newData)
                                .setLink(dataLink.getName())
                                .build();
                        observer.next(serialTerminalData);
                    };

                    dataLink.addListener(listener);
                    cancelListenerList.add(() -> dataLink.removeListener(listener));
                } else {
                    observer.completeExceptionally(new NotFoundException("Could not find serial data link from subscribe serial terminal request " + link));
                }
            } else {
                observer.completeExceptionally(new NotFoundException("Unrecognized data link format from subscribe serial terminal request"));
            }
        }

        observer.setCancelHandler(() -> {
            for(Runnable runnable : cancelListenerList){
                runnable.run();
            }
        });
    }

    @Override
    public void writeSerialTerminal(Context ctx, WriteSerialTerminalRequest request, Observer<Empty> observer) {
        Pattern pattern = Pattern.compile("fc-(\\d+)");

        for (String link : request.getDataLinksList()) {

            Matcher matcher = pattern.matcher(link);
            if (matcher.find() || link.equals("control_box")) {
                String identifer = link.equals("control_box") ? "control_box" : matcher.group(1);
                SerialDataLink dataLink = SerialDataLink.getLinkByIdentifier(identifer);
                if (dataLink != null) {
                    dataLink.writePort(request.getMessage().strip(), null);
                } else {
                    observer.completeExceptionally(new NotFoundException("Could not find serial data link from write serial terminal request " + link));
                }
            } else {
                observer.completeExceptionally(new NotFoundException("Unrecognized data link format from write serial terminal request"));
            }
        }
        observer.complete();
    }
}
