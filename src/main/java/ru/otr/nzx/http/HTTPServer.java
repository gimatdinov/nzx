package ru.otr.nzx.http;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Queue;

import org.littleshoot.proxy.ChainedProxy;
import org.littleshoot.proxy.ChainedProxyAdapter;
import org.littleshoot.proxy.ChainedProxyManager;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.HttpProxyServerBootstrap;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import cxc.jex.tracer.Tracer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import ru.otr.nzx.Server;
import ru.otr.nzx.config.HTTPServerConfig;
import ru.otr.nzx.config.location.LocationConfig;
import ru.otr.nzx.config.location.ProxyPassLocationConfig;
import ru.otr.nzx.http.location.LocationAdapter;

public class HTTPServer extends Server {

    private final HTTPServerConfig config;
    private HttpProxyServerBootstrap srvBootstrap;
    private HttpProxyServer srv;

    public HTTPServer(HTTPServerConfig config, Tracer tracer) {
        super(tracer.getSubtracer(config.name));
        this.config = config;
    }

    @Override
    public void bootstrap() {
        tracer.info("SRV.Bootstrap", "listen " + config.listenHost + ":" + config.listenPort);
        for (LocationConfig item : config.locations.values()) {
            if (item instanceof ProxyPassLocationConfig) {
                ProxyPassLocationConfig loc = (ProxyPassLocationConfig) item;
                if (loc.dump_body_store != null) {
                    File store = new File(loc.dump_body_store);
                    if (!store.exists() && !store.mkdirs()) {
                        tracer.error("SRV.Bootstrap.Error", "Cannot make directory [" + loc.dump_body_store + "]");
                    }
                }
            }
        }

        srvBootstrap = DefaultHttpProxyServer.bootstrap().withName(config.name).withAddress(new InetSocketAddress(config.listenHost, config.listenPort));
        srvBootstrap.withFiltersSource(new HttpFiltersSourceAdapter() {
            @Override
            public int getMaximumRequestBufferSizeInBytes() {
                return config.max_request_buffer_size;
            }

            @Override
            public int getMaximumResponseBufferSizeInBytes() {
                return config.max_response_buffer_size;
            }

            public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                return new LocationAdapter(originalRequest, ctx, config.locations, tracer);
            }
        });

        ChainedProxyManager chainedProxyManager = new ChainedProxyManager() {
            public void lookupChainedProxies(HttpRequest httpRequest, Queue<ChainedProxy> chainedProxies) {
                chainedProxies.add(ChainedProxyAdapter.FALLBACK_TO_DIRECT_CONNECTION);
            }
        };

        srvBootstrap.withChainProxyManager(chainedProxyManager);
    }

    @Override
    public void start() {
        tracer.info("SRV.Start", "");
        srv = srvBootstrap.start();
    }

    @Override
    public void stop() {
        tracer.info("SRV.Stop", "");
        srv.stop();

    }
}
