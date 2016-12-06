package ru.otr.nzx.http;

import java.io.File;
import java.net.InetSocketAddress;

import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.HttpProxyServerBootstrap;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import cxc.jex.tracer.Tracer;
import ru.otr.nzx.config.http.HTTPServerConfig;
import ru.otr.nzx.config.http.location.LocationConfig;
import ru.otr.nzx.postprocessing.NZXPostProcessor;

public class HTTPServer {
    public static enum ObjectType {
        REQ, RES
    }

    private final Tracer tracer;
    private final HTTPServerConfig config;
    private HttpProxyServerBootstrap srvBootstrap;
    private HttpProxyServer srv;
    private NZXPostProcessor postProcessor;

    public HTTPServer(HTTPServerConfig config, Tracer tracer) {
        this.tracer = tracer.getSubtracer(config.getName());
        this.config = config;
    }

    public void bootstrap() {
        tracer.info("Bootstrap", "listen " + config.listenHost + ":" + config.listenPort);
        if (config.post_processing != null && config.post_processing.enable) {
            postProcessor = new NZXPostProcessor(config.post_processing, tracer.getSubtracer("#PostProcessor"));
            postProcessor.bootstrap();
        }
        for (LocationConfig item : config.locations.values()) {
            if (item.enable && item.post_processing_enable) {
                if (postProcessor == null) {
                    throw new RuntimeException("post_processing is not enable, need for location [" + item.path + "]");
                }

                if (item.dump_content_store != null) {
                    File store = new File(item.dump_content_store);
                    if (!store.exists() && !store.mkdirs()) {
                        tracer.error("Bootstrap.Error", "Cannot make directory [" + item.dump_content_store + "]");

                    }
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
        srvBootstrap.withFiltersSource(new HTTPFiltersSource(config, postProcessor, tracer));

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
