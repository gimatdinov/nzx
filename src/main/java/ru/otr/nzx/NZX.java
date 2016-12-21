package ru.otr.nzx;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import cxc.jex.tracer.Tracer;
import ru.otr.nzx.config.model.PostProcessorConfig;
import ru.otr.nzx.config.model.ProcessorConfig;
import ru.otr.nzx.config.model.ServerConfig;
import ru.otr.nzx.config.service.ConfigService;
import ru.otr.nzx.http.postprocessing.NZXPostProcessor;
import ru.otr.nzx.http.processing.Processor;
import ru.otr.nzx.http.server.Server;

public class NZX {
    private final Tracer tracer;
    private Map<String, Server> servers = new HashMap<>();
    private Map<String, Processor> processors = new HashMap<>();
    private Map<String, NZXPostProcessor> postProcessors = new HashMap<>();

    public NZX(ConfigService cfgService, Tracer tracer) {
        this.tracer = tracer.getSubtracer(cfgService.nzx().getServerName());
        for (ServerConfig cfg : cfgService.nzx().http.servers.values()) {
            if (cfg.enable) {
                Server srv = new Server(cfg, processors, postProcessors, this.tracer);
                servers.put(cfg.getName(), srv);
            }
        }
        if (cfgService.nzx().http.processors != null) {
            for (ProcessorConfig cfg : cfgService.nzx().http.processors.values()) {
                if (cfg.enable) {
                    try {
                        @SuppressWarnings("unchecked")
                        Class<Processor> clazz = (Class<Processor>) Class.forName(cfg.processor_class);
                        Constructor<Processor> constructor = clazz.getConstructor(new Class[] { ProcessorConfig.class, Tracer.class });
                        Processor processor = constructor.newInstance(cfg, this.tracer.getSubtracer(cfg.getName()));
                        processors.put(cfg.getName(), processor);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        if (cfgService.nzx().http.post_processors != null) {
            for (PostProcessorConfig cfg : cfgService.nzx().http.post_processors.values()) {
                if (cfg.enable) {
                    NZXPostProcessor postProcessor = new NZXPostProcessor(cfg, processors, this.tracer.getSubtracer(cfg.getName()));
                    postProcessors.put(cfg.getName(), postProcessor);
                }
            }
        }
        if (servers.size() == 0) {
            Thread.currentThread().interrupt();
        }
    }

    public void bootstrap() {
        tracer.info("Bootstrap", "");
        for (Processor item : processors.values()) {
            item.bootstrap();
        }
        for (NZXPostProcessor item : postProcessors.values()) {
            item.bootstrap();
        }
        for (Server item : servers.values()) {
            item.bootstrap();
        }
    }

    public void start() {
        tracer.info("Starting", "");
        for (Processor item : processors.values()) {
            item.start();
        }
        for (NZXPostProcessor item : postProcessors.values()) {
            item.start();
        }
        for (Server item : servers.values()) {
            item.start();
        }
        tracer.info("Started/NOTIFY_ADMIN", "");
    }

    public void stop() {
        for (Server item : servers.values()) {
            item.stop();
        }
        for (NZXPostProcessor item : postProcessors.values()) {
            item.stop();
        }
        for (Processor item : processors.values()) {
            item.stop();
        }
        tracer.info("Stopped/NOTIFY_ADMIN", "");
    }

}
