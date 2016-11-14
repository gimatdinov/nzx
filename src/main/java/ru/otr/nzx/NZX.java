package ru.otr.nzx;

import java.util.ArrayList;
import java.util.List;

import cxc.jex.tracer.Tracer;
import ru.otr.nzx.config.NZXConfig;
import ru.otr.nzx.ftp.FTPServer;
import ru.otr.nzx.config.FTPServerConfig;
import ru.otr.nzx.config.HTTPServerConfig;
import ru.otr.nzx.http.HTTPServer;
import ru.otr.nzx.http.postprocessing.Dumping;
import ru.otr.nzx.http.postprocessing.HTTPPostProcessor;

public class NZX extends Server {

	private final NZXConfig config;

	private final List<FTPServer> ftpServers;

	private HTTPPostProcessor postProcessor;
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
		for (FTPServerConfig cfg : config.ftp.servers) {
			FTPServer ftpServer = new FTPServer(cfg, tracer.getSubtracer("FTP"));
			ftpServer.bootstrap();
			ftpServers.add(ftpServer);
		}
		if (config.http.post_processing != null && config.http.post_processing.enable) {
			postProcessor = new HTTPPostProcessor("PostProcessor", config.http.post_processing, tracer.getSubtracer("HTTP"));
			if (config.http.post_processing.add_dumping) {
				postProcessor.registerAction(new Dumping());
			}
		}
		for (final HTTPServerConfig cfg : config.http.servers) {
			HTTPServer httpServer = new HTTPServer(cfg, postProcessor, tracer.getSubtracer("HTTP"));
			httpServer.bootstrap();
			httpServers.add(httpServer);
		}
	}

	public void start() {
		tracer.info("Starting", "");
		for (FTPServer server : ftpServers) {
			server.start();
		}
		if (config.http.post_processing != null && config.http.post_processing.enable) {
			postProcessor.start();
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
		if (config.http.post_processing != null && config.http.post_processing.enable) {
			postProcessor.stop();
		}
		for (FTPServer server : ftpServers) {
			server.stop();
		}
		tracer.info("Stoped", "");
	}

}
