package ru.otr.nzx;

import java.util.ArrayList;
import java.util.List;

import cxc.jex.tracer.Tracer;
import ru.otr.nzx.config.NZXConfigService;
import ru.otr.nzx.config.ftp.FTPServerConfig;
import ru.otr.nzx.config.http.HTTPServerConfig;
import ru.otr.nzx.ftp.FTPServer;
import ru.otr.nzx.http.HTTPServer;

public class NZX {
    private final Tracer tracer;
    private final NZXConfigService cfgService;

    private List<FTPServer> ftpServers = new ArrayList<>();
    private List<HTTPServer> httpServers = new ArrayList<>();

    public NZX(NZXConfigService cfgService, Tracer tracer) {
        this.tracer = tracer.getSubtracer(cfgService.nzx().getName());
        this.cfgService = cfgService;
    }

    public void bootstrap() {
        tracer.info("Bootstrap", "");
        if (cfgService.nzx().config_service_port > 0) {
            cfgService.bootstrap();
        }
        if (cfgService.nzx().ftp != null) {
            for (FTPServerConfig cfg : cfgService.nzx().ftp.servers) {
                if (cfg.enable) {
                    FTPServer ftpServer = new FTPServer(cfg, tracer);
                    ftpServer.bootstrap();
                    ftpServers.add(ftpServer);
                }
            }
        }
        if (cfgService.nzx().http != null) {
            for (final HTTPServerConfig cfg : cfgService.nzx().http.servers) {
                if (cfg.enable) {
                    HTTPServer httpServer = new HTTPServer(cfg, tracer);
                    httpServer.bootstrap();
                    httpServers.add(httpServer);
                }
            }
        }
        if (ftpServers.size() == 0 && httpServers.size() == 0) {
            Thread.currentThread().interrupt();
        }
    }

    public void start() {
        tracer.info("Starting", "");
        if (cfgService.nzx().config_service_port > 0) {
            cfgService.start();
        }
        for (FTPServer server : ftpServers) {
            server.start();
        }
        for (HTTPServer server : httpServers) {
            server.start();
        }
        tracer.info("Started/NOTIFY_ADMIN", "");
    }

    public void stop() {
        for (HTTPServer server : httpServers) {
            server.stop();
        }
        for (FTPServer server : ftpServers) {
            server.stop();
        }
        if (cfgService.nzx().config_service_port > 0) {
            cfgService.stop();
        }
        tracer.info("Stoped/NOTIFY_ADMIN", "");
    }

}
