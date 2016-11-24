package ru.otr.nzx.http.location;

import java.net.URI;
import java.util.Date;

import org.littleshoot.proxy.HttpFiltersAdapter;

import cxc.jex.postprocessing.PostProcessor;
import cxc.jex.tracer.Tracer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import ru.otr.nzx.config.http.location.LocationConfig;
import ru.otr.nzx.http.HTTPServer.ObjectType;
import ru.otr.nzx.postprocessing.Tank;

public abstract class Location extends HttpFiltersAdapter {

    protected final PostProcessor<Tank> postProcessor;
    protected final Tracer tracer;

    protected final Date requestDateTime;
    protected final String requestID;
    protected final URI requestURI;

    protected final LocationConfig config;

    public Location(HttpRequest originalRequest, ChannelHandlerContext ctx, Date requestDateTime, String requestID, URI requestURI, LocationConfig config,
            PostProcessor<Tank> postProcessor, Tracer tracer) {
        super(originalRequest, ctx);
        this.requestDateTime = requestDateTime;
        this.requestID = requestID;
        this.requestURI = requestURI;
        this.config = config;
        this.postProcessor = postProcessor;
        this.tracer = tracer.getSubtracer(requestID);
    }

    protected void putToPostProcessor(HttpObject httpObject) {
        Tank tank = postProcessor.getEmptyTank();
        tank.requestID = requestID;
        tank.requestDateTime = requestDateTime;
        tank.requestURI = requestURI;
        tank.success = httpObject.getDecoderResult().isSuccess();

        if (httpObject instanceof HttpRequest) {
            tank.type = ObjectType.REQ;
            tank.responseStatusCode = 0;

        }
        if (httpObject instanceof HttpResponse) {
            tank.type = ObjectType.RES;
            tank.responseStatusCode = ((HttpResponse) httpObject).getStatus().code();
        }

        tank.contentLength = 0;
        if (httpObject instanceof FullHttpMessage) {
            FullHttpMessage msg = (FullHttpMessage) httpObject;
            int rix = msg.content().readerIndex();
            tank.contentLength = msg.content().readableBytes();
            tank.properties.put(LocationConfig.DUMP_CONTENT_STORE, config.dump_content_store);
            msg.content().readBytes(tank.data, 0, Math.min(msg.content().readableBytes(), tank.data.length));
            postProcessor.put(tank);
            msg.content().setIndex(rix, msg.content().writerIndex());
        }

    }
}
