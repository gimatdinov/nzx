package ru.otr.nzx.config.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Date;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.HttpProxyServerBootstrap;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import cxc.jex.tracer.Tracer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import ru.otr.nzx.config.Config;
import ru.otr.nzx.config.NZXConfig;
import ru.otr.nzx.config.SimpleConfig;
import ru.otr.nzx.config.http.location.LocationConfig;
import ru.otr.nzx.config.http.location.LocationConfigMap;
import ru.otr.nzx.config.http.location.LocationConfig.LocationType;
import ru.otr.nzx.config.http.postprocessing.ActionConfig;
import ru.otr.nzx.util.NZXUtil;

public class ConfigService {
    private final Tracer tracer;

    public final static String DEFAULT_CONFIG_PATHNAME = "config" + File.separator + "nzx.conf";
    public final static String PROPERTY_NZX_LOG = "nzx_log";
    public final static String SERVICE_NAME = "#ConfigService";

    private final NZXConfig nzx;

    private HttpProxyServerBootstrap srvBootstrap;
    private HttpProxyServer srv;

    public ConfigService(File nzxConfig, Tracer tracer) throws URISyntaxException, ClassNotFoundException, JSONException, IOException {
        this.tracer = tracer.getSubtracer(SERVICE_NAME);
        if (nzxConfig.exists()) {
            tracer.info("Main.Config.File", nzxConfig.getPath());
        } else {
            tracer.error("Main.Config.File.NotFound", nzxConfig.getPath());
            throw new FileNotFoundException(nzxConfig.getPath());
        }

        String[] lines = new String(Files.readAllBytes(nzxConfig.toPath())).split("\n");
        StringBuilder cleanCfg = new StringBuilder();
        for (String line : lines) {
            if (!line.trim().startsWith("//")) {
                cleanCfg.append(line);
                cleanCfg.append("\n");
            }
        }
        this.nzx = new NZXConfig(new JSONObject(cleanCfg.toString()));
        if (nzx.log_config != null) {
            loadLogConfig(nzxConfig.getParentFile().getPath() + File.separator + nzx.log_config, nzx.log);
        }
        tracer.debug("Main.Config.Loaded", nzx.toString());
        tracer.debug("Context", nzx.getContext().keySet().toString());
    }

    public NZXConfig nzx() {
        return nzx;
    }

    public Map<String, Config> getContext() {
        return nzx.getContext();
    }

    private void loadLogConfig(String logConfigFile, String logDir) {
        tracer.info("Log.Config.File", logConfigFile);
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            configurator.getContext().putProperty(PROPERTY_NZX_LOG, logDir);
            configurator.doConfigure(logConfigFile);
        } catch (JoranException je) {
            StatusPrinter.printInCaseOfErrorsOrWarnings(context);
        }
    }

    public void bootstrap() {
        tracer.info("Bootstrap", "listen localhost:" + nzx.config_service_port);
        final ConfigService cfgService = this;
        srvBootstrap = DefaultHttpProxyServer.bootstrap().withName(SERVICE_NAME).withAddress(new InetSocketAddress("localhost", nzx.config_service_port));

        srvBootstrap.withFiltersSource(new HttpFiltersSourceAdapter() {
            public HttpFilters filterRequest(HttpRequest request, ChannelHandlerContext ctx) {
                String requestID = NZXUtil.makeRequestID();
                Date requestDateTime = new Date();
                tracer.info("Request", NZXUtil.requestToLongLine(requestID, request, ctx, tracer.isDebugEnabled()));
                return new ConfigLocation(request, ctx, requestDateTime, requestID, cfgService, tracer);
            }
        });
    }

    public void start() {
        tracer.info("Starting", "");
        srv = srvBootstrap.start();
    }

    public void stop() {
        srv.stop();
        tracer.info("Stopped", "");
    }

    public LocationConfig updateLocation(LocationConfig node, Map<String, String> parameters) throws URISyntaxException {
        synchronized (node) {
            if (parameters.containsKey(LocationConfig.ENABLE)) {
                node.enable = Boolean.valueOf(parameters.get(LocationConfig.ENABLE));
            }
            if (parameters.containsKey(LocationConfig.PROXY_PASS)) {
                node.proxy_pass = new URI(parameters.get(LocationConfig.PROXY_PASS));
                node.type = LocationType.PROXY_PASS;
            }
            tracer.info("Location.Config.Update", node.getPathName() + "=" + node.toString());
            return node;
        }
    }

    public ActionConfig updateAction(ActionConfig node, Map<String, String> parameters) throws URISyntaxException {
        synchronized (node) {
            node.setParameters(parameters);
            tracer.info("Action.Config.Update", node.getPathName() + "=" + node.toString());
            return node;
        }
    }

    public LocationConfigMap createLocation(LocationConfigMap node, Map<String, String> parameters) throws URISyntaxException {
        synchronized (node) {
            String name = parameters.get(LocationConfig.NAME);
            if (name == null || name.length() == 0) {
                throw new IllegalArgumentException("name cannot be empty!");
            }
            String path = parameters.get(LocationConfig.PATH);
            if (path == null || path.length() == 0) {
                throw new IllegalArgumentException("path cannot be empty!");
            }
            new URI(path);
            LocationConfig loc = new LocationConfig(name, path, node);
            tracer.info("Location.Config.Create", loc.getPathName() + "=" + loc.toString());
            return node;
        }
    }

    public void deleteLocation(LocationConfig node) {
        node.delete();
        tracer.info("Location.Config.Delete", node.getPathName() + "=" + node.toString());
    }

    public SimpleConfig update(SimpleConfig node, Map<String, String> parameters) {
        synchronized (node) {
            node.putAll(parameters);
            tracer.info(node.getName() + ".Config.Update", node.getPathName() + "=" + node.toString());
            return node;
        }
    }

    public SimpleConfig delete(SimpleConfig node) {
        node.clear();
        tracer.info(node.getName() + ".Config.Delete", node.getPathName() + "=" + node.toString());
        return node;
    }

}
