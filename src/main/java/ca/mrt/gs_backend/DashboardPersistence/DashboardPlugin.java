package ca.mrt.gs_backend.DashboardPersistence;


import ca.mrt.gs_backend.DashboardPersistence.Api.DashboardApi;
import org.yamcs.Plugin;
import org.yamcs.PluginException;
import org.yamcs.YConfiguration;
import org.yamcs.YamcsServer;
import org.yamcs.http.HttpServer;
import org.yamcs.logging.Log;

public class DashboardPlugin implements Plugin {

    private static final Log log = new Log(DashboardPlugin.class);


    @Override
    public void onLoad(YConfiguration config) throws PluginException {
        YamcsServer yamcs = YamcsServer.getServer();
        HttpServer httpServer = yamcs.getGlobalService(HttpServer.class);
        log.info("Successfully loaded Dashboard plugin");

        if (httpServer == null) {
            log.warn("Can't mount dashboard endpoint. Yamcs does not appear to be running an HTTP Server.");
            return;
        }

        httpServer.addApi(new DashboardApi());
        log.info("Successfully added Dashboard API");



    }

    }








