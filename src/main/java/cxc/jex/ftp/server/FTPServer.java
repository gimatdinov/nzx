package cxc.jex.ftp.server;

import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;

import cxc.jex.tracer.Tracer;

public class FTPServer {
    protected final Tracer tracer;
    protected FTPListener listener;
    protected FtpServer srv;

    public FTPServer(Tracer tracer) {
        this.tracer = tracer;
    }

    public void bootstrap(String host, int port, boolean activeEnable, String passivePorts, String directory, boolean anonymousEnable) {
        tracer.info("Bootstrap", "");
        FTPUserManager ftpUserManager = new FTPUserManager(directory, anonymousEnable);
        FtpServerFactory serverFactory = new FtpServerFactory();
        serverFactory.setUserManager(ftpUserManager);

        DataConnectionConfigurationFactory dcConfigFactory = new DataConnectionConfigurationFactory();
        dcConfigFactory.setActiveEnabled(activeEnable);
        if (passivePorts != null) {
            tracer.info("PassivePorts", passivePorts);
            dcConfigFactory.setPassivePorts(passivePorts);
        }
        listener = new FTPListener(host, port, dcConfigFactory.createDataConnectionConfiguration(), false);
        serverFactory.addListener("default", listener);

        serverFactory.getListeners();
        srv = serverFactory.createServer();
    }

    public void start() {
        tracer.info("Starting", "listen " + listener.getServerAddress() + ":" + listener.getPort());
        try {
            srv.start();
        } catch (FtpException e) {
            tracer.error("Starting.Error", e.getMessage(), e);
        }
    }

    public void stop() {
        srv.stop();
        tracer.info("Stopped", "");
    }

}
