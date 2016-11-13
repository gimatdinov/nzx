package ru.otr.nzx;

import java.util.ArrayList;
import java.util.List;

import cxc.jex.tracer.Tracer;
import ru.otr.nzx.config.NZXConfig;
import ru.otr.nzx.dumper.Dumper;
import ru.otr.nzx.ftp.FTPServer;
import ru.otr.nzx.config.FTPServerConfig;
import ru.otr.nzx.config.HTTPServerConfig;
import ru.otr.nzx.http.HTTPServer;

public class NZX extends Server {

    private final NZXConfig config;
    private Dumper dumper;
    private final List<FTPServer> ftpServers;
    private final List<HTTPServer> httpServers;

    public NZX(NZXConfig config, Tracer tracer) {
        super(tracer.getSubtracer("NZX"));
        this.tracer.debug("Config", config.toString());
        this.config = config;
        this.ftpServers = new ArrayList<>();
        this.httpServers = new ArrayList<>();
    }

    @Override
    public void bootstrap() {
        tracer.info("Bootstrap", "");
        if (config.dumper != null) {
            dumper = new Dumper("Dumper", config.dumper, tracer);
        }
        for (FTPServerConfig cfg : config.ftp.servers) {
            FTPServer ftpServer = new FTPServer(cfg, tracer.getSubtracer("FTP"));
            ftpServer.bootstrap();
            ftpServers.add(ftpServer);
        }
        for (final HTTPServerConfig cfg : config.http.servers) {
            HTTPServer httpServer = new HTTPServer(cfg, dumper, tracer.getSubtracer("HTTP"));
            httpServer.bootstrap();
            httpServers.add(httpServer);
        }
    }

    public void start() {
        tracer.info("Starting", "");
        if (config.dumper != null) {
            dumper.start();
        }
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
        for (FTPServer server : ftpServers) {
            server.stop();
        }
        for (HTTPServer server : httpServers) {
            server.stop();
        }
        if (config.dumper != null) {
            dumper.stop();
        }
        tracer.info("Stoped", "");
    }

}
