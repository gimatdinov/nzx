package ru.otr.nzx.http.processing;

import java.net.URI;
import java.util.Date;

import cxc.jex.tracer.Tracer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import ru.otr.nzx.config.model.ActionConfig;
import ru.otr.nzx.config.model.LocationConfig;
import ru.otr.nzx.config.model.ProcessorConfig;
import ru.otr.nzx.http.postprocessing.NZXAction;
import ru.otr.nzx.http.postprocessing.NZXPostProcessor;
import ru.otr.nzx.http.server.Location;

public abstract class Processor {
    protected final Tracer tracer;
    protected final ProcessorConfig config;

    public Processor(ProcessorConfig config, Tracer tracer) {
        this.config = config;
        this.tracer = tracer;
    }

    public abstract void bootstrap();

    public abstract void start();

    public abstract void stop();

    public abstract Location makeLocation(HttpRequest originalRequest, ChannelHandlerContext ctx, Date requestDateTime, String requestID, URI requestURI,
            LocationConfig locationConfig, NZXPostProcessor postProcessor, Tracer locationTracer);

    public abstract NZXAction makeAction(ActionConfig config);
}
