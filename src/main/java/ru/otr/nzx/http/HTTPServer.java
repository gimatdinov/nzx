package ru.otr.nzx.http;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.HttpProxyServerBootstrap;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import cxc.jex.postprocessing.Action;
import cxc.jex.tracer.Tracer;
import ru.otr.nzx.config.http.HTTPServerConfig;
import ru.otr.nzx.config.http.location.LocationConfig;
import ru.otr.nzx.config.http.location.LocationConfig.LocationType;
import ru.otr.nzx.postprocessing.NZXPostProcessor;
import ru.otr.nzx.postprocessing.NZXTank;

public class HTTPServer {
    public static enum ObjectType {
        REQ, RES
    }

    private final Tracer tracer;
    private final HTTPServerConfig config;
    private HttpProxyServerBootstrap srvBootstrap;
    private HttpProxyServer srv;
    private Map<LocationConfig, Processor> processors;
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
        processors = new HashMap<>();
        for (LocationConfig item : config.locations.values()) {
            if (item.enable && item.post_processing_enable) {
                if (postProcessor == null) {
                    throw new RuntimeException("post_processing not enabled, need for location [" + item.path + "]");
                }

                if (item.dumping_enable) {
                    if (config.post_processing.dumps_store == null) {
                        throw new RuntimeException("dumps_store not setted, need for location [" + item.path + "]");
                    }
                    File store = new File(config.post_processing.dumps_store + "/" + item.getName()).getAbsoluteFile();
                    if (!store.exists() && !store.mkdirs()) {
                        throw new RuntimeException("Cannot make directory [" + store.getPath() + "]");
                    }
                }
            }
            if (item.enable && item.type == LocationType.PROCESSOR) {
                try {
                    @SuppressWarnings("unchecked")
                    Class<Processor> clazz = (Class<Processor>) Class.forName(item.processor_class);
                    Constructor<Processor> constructor = clazz.getConstructor(new Class[] { String.class, LocationConfig.class, NZXPostProcessor.class, Tracer.class });
                    Processor processor = constructor.newInstance(config.getName(), item, postProcessor, tracer);
                    Action<NZXTank> action = processor.makeAction();
                    if (action != null)
                        if (postProcessor != null) {
                            postProcessor.getActions().add(action);
                        } else {
                            throw new RuntimeException(
                                    "post_processing not enabled, need for processor [" + clazz.getName() + "] in location [" + item.path + "]");
                        }
                    processors.put(item, processor);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        }
        for (Processor item : processors.values()) {
            item.bootstrap();
        }
        srvBootstrap = DefaultHttpProxyServer.bootstrap().withName(config.getName()).withAddress(new InetSocketAddress(config.listenHost, config.listenPort));
        if (config.connect_timeout > 0) {
            srvBootstrap.withConnectTimeout(config.connect_timeout);
        }
        if (config.idle_connection_timeout > 0) {
            srvBootstrap.withIdleConnectionTimeout(config.idle_connection_timeout);
        }
        srvBootstrap.withFiltersSource(new HTTPFiltersSource(config, processors, postProcessor, tracer));

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
        for (Processor item : processors.values()) {
            item.start();
        }
        srv = srvBootstrap.start();
    }

    public void stop() {
        srv.stop();
        for (Processor item : processors.values()) {
            item.stop();
        }
        if (postProcessor != null) {
            postProcessor.stop();
        }
        tracer.info("Stopped", "");
    }

}
