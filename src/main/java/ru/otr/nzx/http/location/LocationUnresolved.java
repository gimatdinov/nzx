package ru.otr.nzx.http.location;

import java.net.URI;
import java.util.Date;

import cxc.jex.postprocessing.PostProcessor;
import cxc.jex.tracer.Tracer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import ru.otr.nzx.NZXConstants;
import ru.otr.nzx.config.http.location.LocationConfig;

public class LocationUnresolved extends Location {

    public final static String ANSWER_404 = "<html><head><title>404 Not Found</title></head><body bgcolor=\"white\"><center><h1>404 Not Found</h1></center><hr><center>NZX "
            + NZXConstants.NZX_VERSION + "</center></body></html>";

    public LocationUnresolved(HttpRequest originalRequest, ChannelHandlerContext ctx, Date requestDateTime, String requestID, URI requestURI,
            LocationConfig config, PostProcessor postProcessor, Tracer tracer) {
        super(originalRequest, ctx, requestDateTime, requestID, requestURI, config, postProcessor, tracer);

    }

    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
        this.tracer.info("Request", requestURI.getPath());
        ByteBuf buffer = Unpooled.wrappedBuffer(ANSWER_404.getBytes());
        HttpVersion httpVersion = HttpVersion.HTTP_1_1;
        if (httpObject instanceof HttpRequest) {
            httpVersion = ((HttpRequest) httpObject).getProtocolVersion();
        }
        HttpResponse response = new DefaultFullHttpResponse(httpVersion, HttpResponseStatus.NOT_FOUND, buffer);
        HttpHeaders.setContentLength(response, buffer.readableBytes());
        HttpHeaders.setHeader(response, HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");

        return response;
    }
}
