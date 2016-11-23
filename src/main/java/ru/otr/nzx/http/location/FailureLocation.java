package ru.otr.nzx.http.location;

import java.net.URI;
import java.util.Date;

import cxc.jex.postprocessing.PostProcessor;
import cxc.jex.tracer.Tracer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpHeaders.Values;
import ru.otr.nzx.NZXConstants;
import ru.otr.nzx.config.http.location.LocationConfig;

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
        return makeFailureResponse(responseStatusCode, httpVersion);
    }

    public static FullHttpResponse makeFailureResponse(int responseStatusCode, HttpVersion httpVersion) {
        String content = "<html><head><title>" + HttpResponseStatus.valueOf(responseStatusCode).toString()
                + "</title></head><body bgcolor=\"white\"><center><h1>" + HttpResponseStatus.valueOf(responseStatusCode).toString()
                + "</h1></center><hr><center>NZX " + NZXConstants.NZX_VERSION + "</center></body></html>";
        ByteBuf buffer = Unpooled.wrappedBuffer(content.getBytes());
        FullHttpResponse response = new DefaultFullHttpResponse(httpVersion, HttpResponseStatus.valueOf(responseStatusCode), buffer);
        HttpHeaders.setContentLength(response, buffer.readableBytes());
        HttpHeaders.setHeader(response, HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
        HttpHeaders.setHeader(response, Names.CONNECTION, Values.CLOSE);
        return response;
    }
}
