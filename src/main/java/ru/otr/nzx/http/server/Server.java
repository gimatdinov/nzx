package ru.otr.nzx.http.server;

import java.net.InetSocketAddress;
import java.util.Map;

import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.HttpProxyServerBootstrap;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import cxc.jex.tracer.Tracer;
import ru.otr.nzx.config.model.LocationConfig;
import ru.otr.nzx.config.model.ServerConfig;
import ru.otr.nzx.config.model.LocationConfig.LocationType;
import ru.otr.nzx.http.postprocessing.NZXPostProcessor;
import ru.otr.nzx.http.processing.Processor;

public class Server {
    public static enum ObjectType {
        REQ, RES
    }

    private final Tracer tracer;
    private final ServerConfig config;
    private final Map<String, Processor> processors;
    private final Map<String, NZXPostProcessor> postProcessors;
    private HttpProxyServerBootstrap srvBootstrap;
    private HttpProxyServer srv;

    public Server(ServerConfig config, Map<String, Processor> processors, Map<String, NZXPostProcessor> postProcessors, Tracer tracer) {
        this.tracer = tracer.getSubtracer(config.getName());
        this.config = config;
        this.processors = processors;
        this.postProcessors = postProcessors;
    }

    public void bootstrap() {
        tracer.info("Bootstrap", "");
        for (LocationConfig item : config.locations.values()) {
            if (item.enable) {
                if (item.type == LocationType.PROCESSOR && processors.get(item.processor_name) == null) {
                    throw new RuntimeException(
                            "http.processors[name=\"" + item.processor_name + "\"] not found, need for locations[name=\"" + item.getName() + "\"]");
                }
                if (item.post_processor_name != null && postProcessors.get(item.post_processor_name) == null) {
                    throw new RuntimeException(
                            "http.post_processors[name=\"" + item.post_processor_name + "\"] not found, need for locations[name=\"" + item.getName() + "\"]");
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
        srvBootstrap.withFiltersSource(new LocationRouter(config, processors, postProcessors, tracer));

        // srvBootstrap.withChainProxyManager(new ChainedProxyManager() {
        // public void lookupChainedProxies(HttpRequest httpRequest,
        // Queue<ChainedProxy> chainedProxies) {
        // chainedProxies.add(ChainedProxyAdapter.FALLBACK_TO_DIRECT_CONNECTION);
        // }
        // });

    }

    public void start() {
        tracer.info("Starting", "listen " + config.listenHost + ":" + config.listenPort);
        srv = srvBootstrap.start();
    }

    public void stop() {
        srv.stop();
        tracer.info("Stopped", "");
    }

}
