package ru.otr.nzx.http.server;

import java.net.URI;
import java.util.Date;

import org.littleshoot.proxy.HttpFiltersAdapter;

import cxc.jex.tracer.Tracer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpHeaders.Values;
import ru.otr.nzx.config.model.LocationConfig;
import ru.otr.nzx.http.postprocessing.NZXPostProcessor;
import ru.otr.nzx.http.postprocessing.NZXTank;
import ru.otr.nzx.http.server.Server.ObjectType;

public class Location extends HttpFiltersAdapter {

    protected final NZXPostProcessor postProcessor;
    protected final Tracer tracer;

    protected final Date requestDateTime;
    protected final String requestID;
    protected final URI requestURI;

    protected final LocationConfig config;

    public Location(HttpRequest originalRequest, ChannelHandlerContext ctx, Date requestDateTime, String requestID, URI requestURI, LocationConfig config,
            NZXPostProcessor postProcessor, Tracer tracer) {
        super(originalRequest, ctx);
        this.requestDateTime = requestDateTime;
        this.requestID = requestID;
        this.requestURI = requestURI;
        this.config = config;
        this.postProcessor = postProcessor;
        this.tracer = tracer.getSubtracer(requestID);
    }

    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
        tracer.info("HTTP." + HttpResponseStatus.NO_CONTENT.code(), requestURI.getPath());
        if (httpObject instanceof HttpRequest) {
            putToPostProcessor(httpObject);
        }
        FullHttpResponse response = new DefaultFullHttpResponse(originalRequest.getProtocolVersion(), HttpResponseStatus.NO_CONTENT);
        HttpHeaders.setHeader(response, Names.CONNECTION, Values.CLOSE);
        return response;
    }

    protected void putToPostProcessor(HttpObject httpObject) {
        if (postProcessor != null) {
            NZXTank tank = new NZXTank();
            tank.locationName = config.getName();
            tank.httpMethod = originalRequest.getMethod().name();
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

            if (httpObject instanceof FullHttpMessage) {
                FullHttpMessage msg = (FullHttpMessage) httpObject;
                postProcessor.attachBuffer(tank, msg.content().readableBytes());
                tank.writeContent(msg.content());
            }
            postProcessor.put(tank);
        }
    }
}
