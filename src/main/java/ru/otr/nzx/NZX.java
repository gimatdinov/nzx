package ru.otr.nzx;


import java.util.ArrayList;
import java.util.List;

import cxc.jex.tracer.Tracer;
import ru.otr.nzx.config.NZXConfig;
import ru.otr.nzx.ftp.FTPServer;
import ru.otr.nzx.config.FTPServerConfig;
import ru.otr.nzx.config.HTTPServerConfig;
import ru.otr.nzx.http.HTTPServer;

public class NZX extends Server {

    private final NZXConfig config;
    private final List<FTPServer> ftpServers;
    private final List<HTTPServer> httpServers;

    public NZX(String name, NZXConfig config, Tracer tracer) {
        super(tracer.getSubtracer(name));
        this.config = config;
        this.ftpServers = new ArrayList<>();
        this.httpServers = new ArrayList<>();
    }

    @Override
    public void bootstrap() {
        tracer.info("SRV.Bootstrap", "");
        for (FTPServerConfig cfg : config.ftp.servers) {
            FTPServer ftpServer = new FTPServer(cfg, tracer.getSubtracer("FTP"));
            ftpServer.bootstrap();
            ftpServers.add(ftpServer);
        }
        for (final HTTPServerConfig cfg : config.http.servers) {
            HTTPServer httpServer = new HTTPServer(cfg, tracer.getSubtracer("HTTP"));
            httpServer.bootstrap();
            httpServers.add(httpServer);
        }
    }

    public void start() {
        tracer.info("SRV.Start", "");
        for (FTPServer server : ftpServers) {
            server.start();
        }
        for (HTTPServer server : httpServers) {
            server.start();
        }
    }

    @Override
    public void stop() {
        tracer.info("SRV.Stop", "");
        for (FTPServer server : ftpServers) {
            server.stop();
        }
        for (HTTPServer server : httpServers) {
            server.stop();
        }
    }

}
