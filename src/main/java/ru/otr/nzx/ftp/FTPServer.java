package ru.otr.nzx.ftp;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;

import ru.otr.nzx.Server;
import ru.otr.nzx.config.FTPServerConfig;

public class FTPServer extends Server {
	private FTPServerConfig config;
	private FtpServer srv;

	public FTPServer(String name, FTPServerConfig config) {
		super(name);
		this.config = config;
	}

	@Override
	public void bootstrap() {
		log.info("FTP: server " + config.listenHost + ":" + config.listenPort + " bootstrap...");
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
		log.info("FTP: server " + config.listenHost + ":" + config.listenPort + " start...");
		try {
			srv.start();
		} catch (FtpException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void stop() {
		log.info("FTP: server " + config.listenHost + ":" + config.listenPort + " stop...");
		srv.stop();
	}

}
