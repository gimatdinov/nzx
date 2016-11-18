package ru.otr.nzx.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.UUID;

import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;

import cxc.jex.postprocessing.PostProcessor;
import cxc.jex.tracer.Tracer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import ru.otr.nzx.config.http.HTTPServerConfig;
import ru.otr.nzx.config.http.location.*;
import ru.otr.nzx.http.HTTPServer.ObjectType;
import ru.otr.nzx.http.location.*;

public class HTTPFiltersSource extends HttpFiltersSourceAdapter {

    private final HTTPServerConfig config;
    private final PostProcessor postProcessor;
    private final Tracer tracer;

    public HTTPFiltersSource(HTTPServerConfig config, PostProcessor postProcessor, Tracer tracer) {
        this.config = config;
        this.postProcessor = postProcessor;
        this.tracer = tracer;
    }

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
        logLine.append(ObjectType.REQ);
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
        tracer.info("Request", logLine.toString());

        try {
            URI requestURI = new URI(request.getUri()).normalize();
            LocationConfig location = config.locate(requestURI.getPath());
            if (location != null && location.enable) {
                if (location instanceof ProxyPassLocationConfig) {
                    return new ProxyPassLocation(request, ctx, requestDateTime, requestID, requestURI, (ProxyPassLocationConfig) location, postProcessor,
                            tracer.getSubtracer(location.path));
                }
                return new Location(request, ctx, requestDateTime, requestID, requestURI, null, postProcessor, tracer.getSubtracer(location.path));
            } else {
                return new LocationUnresolved(request, ctx, requestDateTime, requestID, requestURI, null, postProcessor,
                        tracer.getSubtracer("#LocationUnresolved"));
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static String makeRequestID() {
        return UUID.randomUUID().toString().substring(26, 35);
    }

}
