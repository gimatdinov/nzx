package ru.otr.nzx;

import java.util.ArrayList;
import java.util.List;

import cxc.jex.server.Server;
import cxc.jex.tracer.Tracer;
import ru.otr.nzx.config.NZXConfig;
import ru.otr.nzx.config.ftp.FTPServerConfig;
import ru.otr.nzx.config.http.HTTPServerConfig;
import ru.otr.nzx.ftp.FTPServer;
import ru.otr.nzx.http.HTTPServer;

public class NZX extends Server {
    private final NZXConfig config;

    private List<FTPServer> ftpServers = new ArrayList<>();
    private List<HTTPServer> httpServers = new ArrayList<>();

    public NZX(NZXConfig config, Tracer tracer) {
        super(tracer.getSubtracer("NZX"));
        this.config = config;
        this.tracer.debug("Config", config.toString());
    }

    @Override
    public void bootstrap() {
        tracer.info("Bootstrap", "");
        for (FTPServerConfig cfg : config.ftp.servers) {
            if (cfg.enable) {
                FTPServer ftpServer = new FTPServer(cfg, tracer);
                ftpServer.bootstrap();
                ftpServers.add(ftpServer);
            }
        }
        for (final HTTPServerConfig cfg : config.http.servers) {
            if (cfg.enable) {
                HTTPServer httpServer = new HTTPServer(cfg, tracer);
                httpServer.bootstrap();
                httpServers.add(httpServer);
            }
        }
        if (ftpServers.size() == 0 && httpServers.size() == 0) {
            Thread.currentThread().interrupt();
        }
    }

    public void start() {
        tracer.info("Starting", "");
        for (FTPServer server : ftpServers) {
            server.start();
        }
        for (HTTPServer server : httpServers) {
            server.start();
        }
        tracer.info("Started/NOTIFY_ADMIN", "");
    }

    @Override
    public void stop() {
        for (HTTPServer server : httpServers) {
            server.stop();
        }
        for (FTPServer server : ftpServers) {
            server.stop();
        }
        tracer.info("Stoped/NOTIFY_ADMIN", "");
    }

}
