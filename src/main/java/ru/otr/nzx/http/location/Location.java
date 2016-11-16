package ru.otr.nzx.http.location;

import java.net.URI;
import java.util.Date;

import org.littleshoot.proxy.HttpFiltersAdapter;

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
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpHeaders.Values;
import ru.otr.nzx.NZXConstants;
import ru.otr.nzx.Server.ObjectType;
import ru.otr.nzx.config.http.location.LocationConfig;
import ru.otr.nzx.postprocessing.PostProcessor;
import ru.otr.nzx.postprocessing.Tank;

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
        ByteBuf buffer = Unpooled.wrappedBuffer(ANSWER_500.getBytes());
        HttpResponse response = new DefaultFullHttpResponse(((HttpRequest) httpObject).getProtocolVersion(), HttpResponseStatus.NOT_FOUND, buffer);
        HttpHeaders.setContentLength(response, buffer.readableBytes());
        HttpHeaders.setHeader(response, HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
        HttpHeaders.setHeader(response, Names.CONNECTION, Values.CLOSE);
        return response;
    }

    protected void putToPostProcessor(HttpObject httpObject) {
        ByteBuf content = null;
        ObjectType type = null;
        if (httpObject instanceof FullHttpRequest) {
            content = ((FullHttpRequest) httpObject).content();
            type = ObjectType.REQ;
        }
        if (httpObject instanceof FullHttpResponse) {
            content = ((FullHttpResponse) httpObject).content();
            type = ObjectType.RES;
        }

        if (content != null && content.isReadable()) {
            int rix = content.readerIndex();
            Tank tank = postProcessor.getTank();
            tank.type = type;
            tank.requestID = requestID;
            tank.requestDateTime = requestDateTime;
            tank.requestURI = requestURI;
            tank.properties.put(LocationConfig.DUMP_CONTENT_STORE, config.dump_content_store);
            tank.contentLength = content.readableBytes();
            content.readBytes(tank.data, 0, Math.min(content.readableBytes(), tank.data.length));
            postProcessor.put(tank);
            content.setIndex(rix, content.writerIndex());
        }
    }
}
