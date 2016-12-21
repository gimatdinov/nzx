package ru.otr.nzx;

import cxc.jex.tracer.Tracer;
import ru.otr.nzx.config.service.ConfigService;
import ru.otr.nzx.http.HTTP;

public class NZX {
    private final Tracer tracer;
    private final ConfigService cfgService;
    private HTTP http;

    public NZX(ConfigService cfgService, Tracer tracer) {
        this.tracer = tracer.getSubtracer(cfgService.nzx().getServerName());
        this.cfgService = cfgService;
    }

    public void bootstrap() {
        tracer.info("Bootstrap", "");
        if (cfgService.nzx().config_service_port > 0) {
            cfgService.bootstrap();
        }
        if (cfgService.nzx().http != null) {
            http = new HTTP(cfgService.nzx().http, tracer);
            http.bootstrap();
        }
    }

    public void start() {
        tracer.info("Starting", "");
        if (cfgService.nzx().config_service_port > 0) {
            cfgService.start();
        }
        if (http != null) {
            http.start();
        }
        tracer.info("Started/NOTIFY_ADMIN", "");
    }

    public void stop() {
        if (http != null) {
            http.stop();
        }
        if (cfgService.nzx().config_service_port > 0) {
            cfgService.stop();
        }
        tracer.info("Stopped/NOTIFY_ADMIN", "");
    }

}
