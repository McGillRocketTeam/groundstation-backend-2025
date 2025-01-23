package ca.mrt.gs_backend.DashboardPersistence;


import ca.mrt.gs_backend.DashboardPersistence.Api.DashboardApi;
import ca.mrt.gs_backend.DashboardPersistence.Controller.DashboardController;
import ca.mrt.gs_backend.DashboardPersistence.Services.DashboardService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.netty.channel.ChannelHandler;
import org.yamcs.Plugin;
import org.yamcs.PluginException;
import org.yamcs.YConfiguration;
import org.yamcs.YamcsServer;
import org.yamcs.http.HandlerContext;
import org.yamcs.http.HttpHandler;
import org.yamcs.http.HttpServer;
import org.yamcs.logging.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class DashboardPlugin implements Plugin {

    private static final Log log = new Log(DashboardPlugin.class);


    @Override
    public void onLoad(YConfiguration config) throws PluginException {
        YamcsServer yamcs = YamcsServer.getServer();
        HttpServer httpServer = yamcs.getGlobalService(HttpServer.class);

        if (httpServer == null) {
            log.warn("Can't mount dashboard endpoint. Yamcs does not appear to be running an HTTP Server.");
            return;
        }


        httpServer.addApi(new DashboardApi());

        // Prometheus by default expects a /metrics path.
        // Redirect it to /api/prometheus/metrics for convenience
        var dashboardsHandler = new DashboardsHandler();
        httpServer.addRoute("dashboards", () -> dashboardsHandler);


    }

    @ChannelHandler.Sharable
    private static final class DashboardsHandler extends HttpHandler {

        private DashboardService dashboardService;

        @Override
        public boolean requireAuth() {
            return false;
        }

        @Override
        public void handle(HandlerContext handlerContext) {
            String method = handlerContext.getNettyHttpRequest().method().name();

            if (dashboardService == null) {
                dashboardService = new DashboardService();
                dashboardService.init();
            }

            if (method.equalsIgnoreCase("GET")) {

                List<HashMap<String, Object>> dashboards = dashboardService.getAllDashboards();

                // Convert the list of dashboards to JSON
                Gson gson = new Gson();
                GsonBuilder builder = new GsonBuilder();
                JsonObject json = builder.create().fromJson(gson.toJson(dashboards), JsonObject.class);


                // Send the response with the list of dashboards
                handlerContext.sendOK(json);
            }

        }

    }}
