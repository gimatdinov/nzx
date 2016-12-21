package ru.otr.nzx.http.location;

import java.net.URI;
import java.util.Date;

import cxc.jex.tracer.Tracer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import ru.otr.nzx.config.http.location.LocationConfig;
import ru.otr.nzx.http.postprocessing.HTTPPostProcessor;
import ru.otr.nzx.util.NZXUtil;

public class FailureLocation extends Location {

    private final int responseStatusCode;

    public FailureLocation(HttpRequest originalRequest, ChannelHandlerContext ctx, Date requestDateTime, String requestID, URI requestURI,
            LocationConfig config, HTTPPostProcessor postProcessor, Tracer tracer, int responseStatusCode) {
        super(originalRequest, ctx, requestDateTime, requestID, requestURI, config, postProcessor, tracer);
        this.responseStatusCode = responseStatusCode;
    }

    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
        if (responseStatusCode < 500) {
            tracer.info("HTTP." + HttpResponseStatus.valueOf(responseStatusCode).code(), requestURI.getPath());
        } else {
            tracer.error("HTTP." + HttpResponseStatus.valueOf(responseStatusCode).code(), requestURI.getPath());
        }
        return NZXUtil.makeFailureResponse(responseStatusCode, originalRequest.getProtocolVersion());
    }

}