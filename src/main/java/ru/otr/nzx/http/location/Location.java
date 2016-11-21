package ru.otr.nzx.http.location;

import java.net.URI;
import java.util.Date;

import org.littleshoot.proxy.HttpFiltersAdapter;

import cxc.jex.postprocessing.PostProcessor;
import cxc.jex.tracer.Tracer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
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
import ru.otr.nzx.http.HTTPServer.ObjectType;
import ru.otr.nzx.postprocessing.NZXTank;

public class Location extends HttpFiltersAdapter {

    public final static String ANSWER_500 = "<html><head><title>500 Internal Server Error</title></head><body bgcolor=\"white\"><center><h1>500 Internal Server Error</h1></center><hr><center>NZX "
            + NZXConstants.NZX_VERSION + "</center></body></html>";

    protected final PostProcessor postProcessor;
    protected final Tracer tracer;

    protected final Date requestDateTime;
    protected final String requestID;
    protected final URI requestURI;

    protected final LocationConfig config;

    public Location(HttpRequest originalRequest, ChannelHandlerContext ctx, Date requestDateTime, String requestID, URI requestURI, LocationConfig config,
            PostProcessor postProcessor, Tracer tracer) {
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
        this.tracer.error("Request", "UnsupportedLocationConfig " + config.getClass().getName());
        HttpVersion httpVersion = HttpVersion.HTTP_1_1;
        if (httpObject instanceof HttpRequest) {
            httpVersion = ((HttpRequest) httpObject).getProtocolVersion();
        }
        ByteBuf buffer = Unpooled.wrappedBuffer(ANSWER_500.getBytes());
        HttpResponse response = new DefaultFullHttpResponse(httpVersion, HttpResponseStatus.INTERNAL_SERVER_ERROR, buffer);
        HttpHeaders.setContentLength(response, buffer.readableBytes());
        HttpHeaders.setHeader(response, HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
        HttpHeaders.setHeader(response, Names.CONNECTION, Values.CLOSE);
        return response;
    }

    protected void putToPostProcessor(HttpObject httpObject) {
        ByteBuf content = null;
        ObjectType type = null;
        int responseStatusCode = 0;
        boolean success = false;
        if (httpObject instanceof FullHttpRequest) {
            content = ((FullHttpRequest) httpObject).content();
            type = ObjectType.REQ;
            responseStatusCode = 0;
            success = ((FullHttpRequest) httpObject).getDecoderResult().isSuccess();
        }
        if (httpObject instanceof FullHttpResponse) {
            content = ((FullHttpResponse) httpObject).content();
            type = ObjectType.RES;
            responseStatusCode = ((FullHttpResponse) httpObject).getStatus().code();
            success = ((FullHttpResponse) httpObject).getDecoderResult().isSuccess();
        }

        if (content != null && content.isReadable()) {
            int rix = content.readerIndex();
            NZXTank tank = (NZXTank) postProcessor.getTank(content.readableBytes());
            tank.type = type;
            tank.requestID = requestID;
            tank.requestDateTime = requestDateTime;
            tank.requestURI = requestURI;
            tank.responseStatusCode = responseStatusCode;
            tank.success = success;
            tank.contentLength = content.readableBytes();
            tank.properties.put(LocationConfig.DUMP_CONTENT_STORE, config.dump_content_store);
            content.readBytes(tank.getData(), 0, Math.min(content.readableBytes(), tank.getData().length));
            postProcessor.put(tank);
            content.setIndex(rix, content.writerIndex());
        }
    }
}
