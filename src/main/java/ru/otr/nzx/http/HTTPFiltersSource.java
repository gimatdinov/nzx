package ru.otr.nzx.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Map;

import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;

import cxc.jex.tracer.Tracer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import ru.otr.nzx.config.http.HTTPServerConfig;
import ru.otr.nzx.config.http.location.*;
import ru.otr.nzx.http.location.*;
import ru.otr.nzx.postprocessing.NZXPostProcessor;
import ru.otr.nzx.util.NZXUtil;

public class HTTPFiltersSource extends HttpFiltersSourceAdapter {

    private final HTTPServerConfig config;
    private final Map<LocationConfig, Processor> processors;
    private final NZXPostProcessor postProcessor;
    private final Tracer tracer;

    public HTTPFiltersSource(HTTPServerConfig config, Map<LocationConfig, Processor> processors, NZXPostProcessor postProcessor, Tracer tracer) {
        this.config = config;
        this.processors = processors;
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
        String requestID = NZXUtil.makeRequestID();
        Date requestDateTime = new Date();
        tracer.info("Request", NZXUtil.requestToLongLine(requestID, request, ctx, tracer.isDebugEnabled()));
        try {
            URI requestURI = new URI(request.getUri()).normalize();
            if (!HttpMethod.CONNECT.equals(request.getMethod())) {
                LocationConfig locCfg = config.locate(requestURI.getPath());
                if (locCfg != null && locCfg.enable) {
                    switch (locCfg.type) {
                    case PROXY_PASS:
                        return new ProxyPassLocation(request, ctx, requestDateTime, requestID, requestURI, locCfg, postProcessor,
                                tracer.getSubtracer(locCfg.getName()));
                    case FILE:
                        return new FileLocation(request, ctx, requestDateTime, requestID, requestURI, locCfg, postProcessor,
                                tracer.getSubtracer(locCfg.getName()));
                    case PROCESSOR:
                        return processors.get(locCfg).makeLocation(request, ctx, requestDateTime, requestID, requestURI);
                    default:
                        return new Location(request, ctx, requestDateTime, requestID, requestURI, locCfg, postProcessor, tracer.getSubtracer(locCfg.getName()));
                    }
                } else {
                    return new FailureLocation(request, ctx, requestDateTime, requestID, requestURI, null, postProcessor, tracer.getSubtracer("#NotFound"),
                            404);
                }
            } else {
                return new FailureLocation(request, ctx, requestDateTime, requestID, requestURI, null, postProcessor, tracer.getSubtracer("#MethodNotAllowed"),
                        405);
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
