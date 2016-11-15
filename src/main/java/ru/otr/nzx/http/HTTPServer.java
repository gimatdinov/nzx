package ru.otr.nzx.http;

import java.io.File;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
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
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import ru.otr.nzx.Server;
import ru.otr.nzx.config.NZXConfigHelper;
import ru.otr.nzx.config.http.HTTPServerConfig;
import ru.otr.nzx.config.http.location.LocationConfig;
import ru.otr.nzx.config.http.location.ProxyPassLocationConfig;
import ru.otr.nzx.http.location.LocationAdapter;
import ru.otr.nzx.http.postprocessing.HTTPPostProcessor;

public class HTTPServer extends Server {

    private final HTTPServerConfig config;
    private HttpProxyServerBootstrap srvBootstrap;
    private HttpProxyServer srv;

    private HTTPPostProcessor postProcessor;

    public HTTPServer(HTTPServerConfig config, HTTPPostProcessor postProcessor, Tracer tracer) {
        super(tracer.getSubtracer(config.name));
        this.config = config;
        this.postProcessor = postProcessor;
    }

    @Override
    public void bootstrap() {
        tracer.info("Bootstrap", "listen " + config.listenHost + ":" + config.listenPort);
        for (LocationConfig item : config.locations.values()) {
            if (item instanceof ProxyPassLocationConfig) {
                ProxyPassLocationConfig loc = (ProxyPassLocationConfig) item;
                if (loc.post_processing_enable) {
                    if (postProcessor == null) {
                        throw new RuntimeException("http.post_processing is not enable, need for location [" + item.path + "]");
                    }

                    if (loc.dump_content_store != null) {
                        File store = new File(loc.dump_content_store);
                        if (!store.exists() && !store.mkdirs()) {
                            tracer.error("Bootstrap.Error", "Cannot make directory [" + loc.dump_content_store + "]");

                        }
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

            public HttpFilters filterRequest(HttpRequest request, ChannelHandlerContext ctx) {
                String requestID = makeRequestID();
                Date requestDateTime = new Date();
                StringBuilder logLine = new StringBuilder();
                logLine.append("ID=" + requestID);
                logLine.append(" ");
                logLine.append(request.getUri());
                logLine.append(" ");
                logLine.append("REQ");
                logLine.append(" ");
                logLine.append("LEN=" + request.headers().get(HttpHeaders.Names.CONTENT_LENGTH));
                logLine.append(" ");
                logLine.append(request.getProtocolVersion().toString());
                logLine.append(" ");
                logLine.append(request.getMethod().name());
                logLine.append(" ");
                logLine.append("from " + ctx.channel().remoteAddress().toString());
                logLine.append(" ");
                logLine.append(request.getDecoderResult().toString());
                if (tracer.isDebugEnabled()) {
                    logLine.append("\n");
                    logLine.append("headers=" + request.headers().entries());
                }
                tracer.info("Filter.Request", logLine.toString());

                try {
                    URI requestURI = new URI(request.getUri()).normalize();
                    LocationConfig location = NZXConfigHelper.locate(requestURI.getPath(), config.locations);
                    return new LocationAdapter(request, ctx, requestDateTime, requestID, requestURI, location, postProcessor, tracer);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
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
        tracer.info("Starting", "");
        srv = srvBootstrap.start();
    }

    @Override
    public void stop() {
        srv.stop();
        tracer.info("Stoped", "");
    }

    protected static String makeRequestID() {
        String result = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update((System.currentTimeMillis() + "-" + System.nanoTime()).getBytes());
            result = new BigInteger(1, md.digest()).toString(16);
            while (result.length() < 32) {
                result = "0" + result;
            }
            result = result.substring(0, 10);
        } catch (NoSuchAlgorithmException e) {
        }
        return result;
    }
}
