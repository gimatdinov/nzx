package ru.otr.nzx.http.location;

import java.net.URI;
import java.util.Date;

import cxc.jex.postprocessing.PostProcessor;
import cxc.jex.tracer.Tracer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import ru.otr.nzx.config.http.location.LocationConfig;
import ru.otr.nzx.util.NZXUtil;

public class FailureLocation extends Location {

    private final int responseStatusCode;

    public FailureLocation(HttpRequest originalRequest, ChannelHandlerContext ctx, Date requestDateTime, String requestID, URI requestURI,
            LocationConfig config, PostProcessor postProcessor, Tracer tracer, int responseStatusCode) {
        super(originalRequest, ctx, requestDateTime, requestID, requestURI, config, postProcessor, tracer);
        this.responseStatusCode = responseStatusCode;
    }

    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
        if (responseStatusCode < 500) {
            this.tracer.info(HttpResponseStatus.valueOf(responseStatusCode).toString(), requestURI.getPath());
        } else {
            this.tracer.error(HttpResponseStatus.valueOf(responseStatusCode).toString(), "LocationConfig=" + config);
        }
        HttpVersion httpVersion = HttpVersion.HTTP_1_1;
        if (httpObject instanceof HttpRequest) {
            httpVersion = ((HttpRequest) httpObject).getProtocolVersion();
        }
        return NZXUtil.makeFailureResponse(responseStatusCode, httpVersion);
    }

}