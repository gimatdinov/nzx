package ru.otr.nzx.http;

import java.net.URI;
import java.util.Date;

import cxc.jex.postprocessing.Action;
import cxc.jex.tracer.Tracer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import ru.otr.nzx.config.http.location.LocationConfig;
import ru.otr.nzx.http.location.Location;
import ru.otr.nzx.postprocessing.NZXPostProcessor;
import ru.otr.nzx.postprocessing.NZXTank;

public abstract class Processor {
    protected final LocationConfig locationConfig;
    protected final NZXPostProcessor postProcessor;
    protected final Tracer tracer;

    public Processor(LocationConfig locationConfig, NZXPostProcessor postProcessor, Tracer tracer) {
        this.locationConfig = locationConfig;
        this.postProcessor = postProcessor;
        this.tracer = tracer;
    }

    public abstract Location makeLocation(HttpRequest originalRequest, ChannelHandlerContext ctx, Date requestDateTime, String requestID, URI requestURI);

    public abstract void bootstrap();
    
    public abstract void start();
    
    public abstract void stop();
    
    public abstract Action<NZXTank> makeAction();
}
