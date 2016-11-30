package ru.otr.nzx.ftp;

import cxc.jex.tracer.Tracer;
import ru.otr.nzx.config.ftp.FTPServerConfig;

public class FTPServer extends cxc.jex.ftp.server.FTPServer {
    private FTPServerConfig config;

    public FTPServer(FTPServerConfig config, Tracer tracer) {
        super(tracer.getSubtracer(config.name));
        this.config = config;
    }

    public void bootstrap() {
        init(config.listenHost, config.listenPort, config.active_enable, config.passive_ports, config.directory, config.anonymous_enable);
    }

}
