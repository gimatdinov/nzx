package ru.otr.nzx;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import ru.otr.nzx.config.NZXConfig;
import ru.otr.nzx.ftp.FTPServer;
import ru.otr.nzx.config.FTPServerConfig;
import ru.otr.nzx.config.HTTPServerConfig;
import ru.otr.nzx.http.HTTPServer;

public class NZX extends Server {

	private final NZXConfig config;
	private final List<FTPServer> ftpServers;
	private final List<HTTPServer> httpServers;

	public NZX(String name, NZXConfig config) {
		super(name);
		this.config = config;
		this.ftpServers = new ArrayList<>();
		this.httpServers = new ArrayList<>();
	}

	public static void main(String[] args) {
		try {
			if (args.length != 2) {
				throw new IllegalArgumentException("Usage: java ru.otr.nzx.NZX <name> <path to nzx.conf>");
			}
			String name = args[0];
			File configFile = new File(args[1]).getAbsoluteFile();
			String config = new String(Files.readAllBytes(configFile.toPath()));
			NZX nzx = new NZX(name, new NZXConfig(config));
			nzx.log.debug("config [" + configFile + "]=" + config);
			nzx.bootstrap();
			nzx.start();
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void bootstrap() {
		log.info("ROOT: bootstrap...");
		for (FTPServerConfig serverConfig : config.ftp.servers) {
			FTPServer ftpServer = new FTPServer(name, serverConfig);
			ftpServer.bootstrap();
			ftpServers.add(ftpServer);
		}
		for (final HTTPServerConfig serverConfig : config.http.servers) {
			HTTPServer httpServer = new HTTPServer(name, serverConfig);
			httpServer.bootstrap();
			httpServers.add(httpServer);
		}
	}

	public void start() {
		log.info("ROOT: start...");
		for (FTPServer server : ftpServers) {
			server.start();
		}
		for (HTTPServer server : httpServers) {
			server.start();
		}
	}

	@Override
	public void stop() {
		log.info("ROOT: stop...");
		for (FTPServer server : ftpServers) {
			server.stop();
		}
		for (HTTPServer server : httpServers) {
			server.stop();
		}
	}

}
