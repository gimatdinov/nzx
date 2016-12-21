package ru.otr.nzx.http.server;

import java.net.InetSocketAddress;
import java.util.Map;

import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.HttpProxyServerBootstrap;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import cxc.jex.tracer.Tracer;
import ru.otr.nzx.config.http.location.LocationConfig;
import ru.otr.nzx.config.http.location.LocationConfig.LocationType;
import ru.otr.nzx.config.http.server.HTTPServerConfig;
import ru.otr.nzx.http.postprocessing.HTTPPostProcessor;
import ru.otr.nzx.http.processing.HTTPProcessor;

public class HTTPServer {
    public static enum ObjectType {
        REQ, RES
    }

    private final Tracer tracer;
    private final HTTPServerConfig config;
    private final Map<String, HTTPProcessor> processors;
    private HttpProxyServerBootstrap srvBootstrap;
    private HttpProxyServer srv;
    private HTTPPostProcessor postProcessor;

    public HTTPServer(HTTPServerConfig config, Map<String, HTTPProcessor> processors, Tracer tracer) {
        this.tracer = tracer.getSubtracer(config.getName());
        this.processors = processors;
        this.config = config;
        if (config.post_processing != null && config.post_processing.enable) {
            postProcessor = new HTTPPostProcessor(config.post_processing, processors, this.tracer.getSubtracer("#PostProcessor"));
        }
    }

    public void bootstrap() {
        tracer.info("Bootstrap", "listen " + config.listenHost + ":" + config.listenPort);
        if (postProcessor != null) {
            postProcessor.bootstrap();
        }
        for (LocationConfig item : config.locations.values()) {
            if (item.enable) {
                if (item.type == LocationType.PROCESSOR && processors.get(item.processor_name) == null) {
                    throw new RuntimeException("HTTPProcessor with name \"" + item.processor_name + "\" not found, need for location [" + item.path + "]");
                }
                if (item.post_processing_enable && postProcessor == null) {
                    throw new RuntimeException("post_processing not enabled, need for location [" + item.path + "]");
                }
            }
        }
        srvBootstrap = DefaultHttpProxyServer.bootstrap().withName(config.getName()).withAddress(new InetSocketAddress(config.listenHost, config.listenPort));
        if (config.connect_timeout > 0) {
            srvBootstrap.withConnectTimeout(config.connect_timeout);
        }
        if (config.idle_connection_timeout > 0) {
            srvBootstrap.withIdleConnectionTimeout(config.idle_connection_timeout);
        }
        srvBootstrap.withFiltersSource(new LocationRouter(config, processors, postProcessor, tracer));

        // srvBootstrap.withChainProxyManager(new ChainedProxyManager() {
        // public void lookupChainedProxies(HttpRequest httpRequest,
        // Queue<ChainedProxy> chainedProxies) {
        // chainedProxies.add(ChainedProxyAdapter.FALLBACK_TO_DIRECT_CONNECTION);
        // }
        // });

    }

    public void start() {
        tracer.info("Starting", "");
        if (postProcessor != null) {
            postProcessor.start();
        }
        srv = srvBootstrap.start();
    }

    public void stop() {
        srv.stop();
        if (postProcessor != null) {
            postProcessor.stop();
        }
        tracer.info("Stopped", "");
    }

}
