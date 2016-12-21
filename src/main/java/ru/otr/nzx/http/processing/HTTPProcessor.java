package ru.otr.nzx.http.processing;

import java.net.URI;
import java.util.Date;

import cxc.jex.tracer.Tracer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import ru.otr.nzx.config.http.location.LocationConfig;
import ru.otr.nzx.config.http.postprocessing.ActionConfig;
import ru.otr.nzx.config.http.processing.HTTPProcessorConfig;
import ru.otr.nzx.http.location.Location;
import ru.otr.nzx.http.postprocessing.HTTPMessageAction;
import ru.otr.nzx.http.postprocessing.HTTPPostProcessor;

public abstract class HTTPProcessor {
    protected final Tracer tracer;
    protected final HTTPProcessorConfig config;

    public HTTPProcessor(HTTPProcessorConfig config, Tracer tracer) {
        this.config = config;
        this.tracer = tracer;
    }

    public abstract void bootstrap();

    public abstract void start();

    public abstract void stop();

    public abstract Location makeLocation(HttpRequest originalRequest, ChannelHandlerContext ctx, Date requestDateTime, String requestID, URI requestURI,
            LocationConfig locationConfig, HTTPPostProcessor postProcessor, Tracer locationTracer);

    public abstract HTTPMessageAction makeAction(ActionConfig config);
}
