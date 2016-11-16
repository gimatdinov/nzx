package ru.otr.nzx.http;

import java.io.File;
import java.net.InetSocketAddress;

import java.util.Queue;

import org.littleshoot.proxy.ChainedProxy;
import org.littleshoot.proxy.ChainedProxyAdapter;
import org.littleshoot.proxy.ChainedProxyManager;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.HttpProxyServerBootstrap;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import cxc.jex.tracer.Tracer;
import io.netty.handler.codec.http.HttpRequest;
import ru.otr.nzx.Server;
import ru.otr.nzx.config.http.HTTPServerConfig;
import ru.otr.nzx.config.http.location.LocationConfig;
import ru.otr.nzx.postprocessing.PostProcessor;

public class HTTPServer extends Server {
	private final HTTPServerConfig config;
	private HttpProxyServerBootstrap srvBootstrap;
	private HttpProxyServer srv;
	private PostProcessor postProcessor;

	public HTTPServer(HTTPServerConfig config, Tracer tracer) {
		super(tracer.getSubtracer(config.name));
		this.config = config;
	}

	@Override
	public void bootstrap() {
		tracer.info("Bootstrap", "listen " + config.listenHost + ":" + config.listenPort);
        if (config.post_processing != null && config.post_processing.enable) {
        	int bufferSize= Math.max(config.max_request_buffer_size, config.max_response_buffer_size);
            postProcessor = new PostProcessor("#PostProcessor", config.post_processing, bufferSize, tracer);
            postProcessor.bootstrap();
        }
		for (LocationConfig item : config.locations.values()) {
			if (item.post_processing_enable) {
				if (postProcessor == null) {
					throw new RuntimeException("post_processing is not enable, need for location [" + item.path + "]");
				}

				if (item.dump_content_store != null) {
					File store = new File(item.dump_content_store);
					if (!store.exists() && !store.mkdirs()) {
						tracer.error("Bootstrap.Error", "Cannot make directory [" + item.dump_content_store + "]");

					}
				}
			}
		}

		srvBootstrap = DefaultHttpProxyServer.bootstrap().withName(config.name).withAddress(new InetSocketAddress(config.listenHost, config.listenPort));
		srvBootstrap.withFiltersSource(new HTTPFiltersSource(config, postProcessor, tracer));
		srvBootstrap.withChainProxyManager(new ChainedProxyManager() {
			public void lookupChainedProxies(HttpRequest httpRequest, Queue<ChainedProxy> chainedProxies) {
				chainedProxies.add(ChainedProxyAdapter.FALLBACK_TO_DIRECT_CONNECTION);
			}
		});
	}

	@Override
	public void start() {
		tracer.info("Starting", "");
        if (postProcessor != null) {
            postProcessor.start();
        }
		srv = srvBootstrap.start();
	}

	@Override
	public void stop() {
		srv.stop();
        if (postProcessor != null) {
            postProcessor.stop();
        }
		tracer.info("Stoped", "");
	}

}
