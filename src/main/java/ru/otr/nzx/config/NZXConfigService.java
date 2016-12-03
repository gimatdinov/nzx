package ru.otr.nzx.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Date;
import java.util.Map;

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
import ru.otr.nzx.util.NZXUtil;

public class NZXConfigService {
    private final Tracer tracer;

    public final static String DEFAULT_CONFIG_PATHNAME = "config" + File.separator + "nzx.conf";
    public final static String PROPERTY_NZX_LOG = "nzx_log";
    public final static String SERVICE_NAME = "#ConfigService";

    private final NZXConfig nzx;

    private HttpProxyServerBootstrap srvBootstrap;
    private HttpProxyServer srv;

    public NZXConfigService(File nzxConfig, Tracer tracer) throws URISyntaxException, IOException {
        this.tracer = tracer.getSubtracer(SERVICE_NAME);
        if (nzxConfig.exists()) {
            tracer.info("Config.File", nzxConfig.getPath());
        } else {
            tracer.error("Config.File.NotFound", nzxConfig.getPath());
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
        tracer.debug("Config.Loaded", nzx.toString());
        tracer.debug("Config.Context", nzx.getContext().keySet().toString());
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
        final NZXConfigService cfgService = this;
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
        tracer.info("Stoped", "");
    }

}
