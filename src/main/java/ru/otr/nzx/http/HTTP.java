package ru.otr.nzx.http;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import cxc.jex.tracer.Tracer;
import ru.otr.nzx.config.http.HTTPConfig;
import ru.otr.nzx.config.http.processing.HTTPProcessorConfig;
import ru.otr.nzx.config.http.server.HTTPServerConfig;
import ru.otr.nzx.http.processing.HTTPProcessor;
import ru.otr.nzx.http.server.HTTPServer;

public class HTTP {
    private final Tracer tracer;
    private final HTTPConfig config;

    private Map<String, HTTPProcessor> processors = new HashMap<>();
    private Map<String, HTTPServer> servers = new HashMap<>();

    public HTTP(HTTPConfig config, Tracer tracer) {
        this.config = config;
        this.tracer = tracer.getSubtracer("http");

    }

    public void bootstrap() {
        tracer.info("Bootstrap", "");
        if (config.processors != null) {
            for (HTTPProcessorConfig cfg : config.processors.values()) {
                if (cfg.enable) {
                    try {
                        @SuppressWarnings("unchecked")
                        Class<HTTPProcessor> clazz = (Class<HTTPProcessor>) Class.forName(cfg.processor_class);
                        Constructor<HTTPProcessor> constructor = clazz.getConstructor(new Class[] { HTTPProcessorConfig.class, Tracer.class });
                        HTTPProcessor processor = constructor.newInstance(cfg, tracer.getSubtracer("#" + cfg.getName()));
                        processor.bootstrap();
                        processors.put(cfg.getName(), processor);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        for (final HTTPServerConfig cfg : config.servers.values()) {
            if (cfg.enable) {
                HTTPServer srv = new HTTPServer(cfg, processors, tracer);
                srv.bootstrap();
                servers.put(cfg.getName(), srv);
            }
        }
        if (servers.size() == 0) {
            Thread.currentThread().interrupt();
        }
    }

    public void start() {
        tracer.info("Starting", "");
        for (HTTPProcessor proc : processors.values()) {
            proc.start();
        }
        for (HTTPServer srv : servers.values()) {
            srv.start();
        }
    }

    public void stop() {
        for (HTTPServer srv : servers.values()) {
            srv.stop();
        }
        for (HTTPProcessor proc : processors.values()) {
            proc.stop();
        }
        tracer.info("Stopped", "");
    }
}
