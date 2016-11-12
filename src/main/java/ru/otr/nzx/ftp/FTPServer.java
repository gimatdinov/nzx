package ru.otr.nzx.ftp;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;

import cxc.jex.tracer.Tracer;
import ru.otr.nzx.Server;
import ru.otr.nzx.config.FTPServerConfig;

public class FTPServer extends Server {
    private FTPServerConfig config;
    private FtpServer srv;

    public FTPServer(FTPServerConfig config, Tracer tracer) {
        super(tracer.getSubtracer(config.name));
        this.config = config;
    }

    @Override
    public void bootstrap() {
        tracer.info("SRV.Bootstrap", "listen " + config.listenHost + ":" + config.listenPort);
        FTPUserManager ftpUserManager = new FTPUserManager(config.directory, config.anonymous_enable);
        FtpServerFactory serverFactory = new FtpServerFactory();
        serverFactory.setUserManager(ftpUserManager);

        FTPListener ftpListener = new FTPListener(config.listenHost, config.listenPort, false);
        serverFactory.addListener("default", ftpListener);

        serverFactory.getListeners();
        srv = serverFactory.createServer();
    }

    @Override
    public void start() {
        tracer.info("SRV.Start", "");
        try {
            srv.start();
        } catch (FtpException e) {
            tracer.error("SRV.Start.Error", e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        tracer.info("SRV.Stop", "");
        srv.stop();
    }

}
