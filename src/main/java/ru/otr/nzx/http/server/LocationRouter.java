package ru.otr.nzx.http.server;

import java.lang.reflect.Constructor;
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
import ru.otr.nzx.config.model.LocationConfig;
import ru.otr.nzx.config.model.ServerConfig;
import ru.otr.nzx.http.postprocessing.NZXPostProcessor;
import ru.otr.nzx.http.processing.Processor;
import ru.otr.nzx.util.NZXUtil;

public class LocationRouter extends HttpFiltersSourceAdapter {

    private final ServerConfig config;
    private final Map<String, Processor> processors;
    private final Map<String, NZXPostProcessor> postProcessors;
    private final Tracer tracer;

    public LocationRouter(ServerConfig config, Map<String, Processor> processors, Map<String, NZXPostProcessor> postProcessors, Tracer tracer) {
        this.config = config;
        this.processors = processors;
        this.postProcessors = postProcessors;
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
                    NZXPostProcessor postProcessor = postProcessors.get(locCfg.post_processor_name);
                    switch (locCfg.type) {
                    case PROXY_PASS:
                        return new ProxyPassLocation(request, ctx, requestDateTime, requestID, requestURI, locCfg, postProcessor,
                                tracer.getSubtracer(locCfg.getName()));
                    case FILE:
                        return new FileLocation(request, ctx, requestDateTime, requestID, requestURI, locCfg, postProcessor,
                                tracer.getSubtracer(locCfg.getName()));
                    case CLASS:
                        try {
                            @SuppressWarnings("unchecked")
                            Class<Location> clazz = (Class<Location>) Class.forName(locCfg.location_class);
                            Constructor<Location> constructor = clazz.getConstructor(new Class[] { HttpRequest.class, ChannelHandlerContext.class, Date.class,
                                    String.class, URI.class, LocationConfig.class, NZXPostProcessor.class, Tracer.class });
                            Location location = constructor.newInstance(request, ctx, requestDateTime, requestID, requestURI, locCfg, postProcessor,
                                    tracer.getSubtracer(locCfg.getName()));
                            return location;
                        } catch (Exception e) {
                            tracer.error("Location.Init.Error/NOTIFY_ADMIN", locCfg.toString(), e);
                            return new FailureLocation(request, ctx, requestDateTime, requestID, requestURI, null, null, tracer.getSubtracer(locCfg.getName()),
                                    500);
                        }
                    case PROCESSOR:
                        return processors.get(locCfg.processor_name).makeLocation(request, ctx, requestDateTime, requestID, requestURI, locCfg, postProcessor,
                                tracer.getSubtracer(locCfg.getName()));
                    default:
                        return new Location(request, ctx, requestDateTime, requestID, requestURI, locCfg, postProcessor, tracer.getSubtracer(locCfg.getName()));
                    }
                } else {
                    return new FailureLocation(request, ctx, requestDateTime, requestID, requestURI, null, null, tracer.getSubtracer("#NotFound"), 404);
                }
            } else {
                return new FailureLocation(request, ctx, requestDateTime, requestID, requestURI, null, null, tracer.getSubtracer("#MethodNotAllowed"), 405);
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
