package ru.otr.nzx.http.location;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Map;

import org.littleshoot.proxy.HttpFiltersAdapter;

import cxc.jex.tracer.Tracer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import ru.otr.nzx.NZXConstants;
import ru.otr.nzx.config.http.location.LocationConfig;
import ru.otr.nzx.config.http.location.ProxyPassLocationConfig;
import ru.otr.nzx.http.postprocessing.HTTPPostProcessor;
import ru.otr.nzx.http.postprocessing.Tank;
import ru.otr.nzx.http.postprocessing.Tank.Type;

public class LocationAdapter extends HttpFiltersAdapter {
    private final HTTPPostProcessor postProcessor;
    private final Tracer tracer;

    private final Date requestDateTime;
    private final String requestID;
    private final URI requestURI;
    private final LocationConfig location;

    private final URI passURI;

    public LocationAdapter(HttpRequest originalRequest, ChannelHandlerContext ctx, Date requestDateTime, String requestID, URI requestURI,
            LocationConfig location, HTTPPostProcessor postProcessor, Tracer tracer) {
        super(originalRequest, ctx);
        this.requestDateTime = requestDateTime;
        this.requestID = requestID;
        this.requestURI = requestURI;
        this.postProcessor = postProcessor;
        this.location = location;

        try {
            if (location != null && location instanceof ProxyPassLocationConfig) {
                this.tracer = tracer.getSubtracer(location.path).getSubtracer(requestID);
                this.passURI = makePassURI(requestURI, (ProxyPassLocationConfig) location);
                this.tracer.info("Client.ProxyPass", passURI.getPath());
            } else {
                this.tracer = tracer.getSubtracer("~").getSubtracer(requestID);
                this.passURI = null;
                this.tracer.info("Client.UnresolvedRequest", requestURI.getPath());
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
        HttpResponse response = null;
        if (httpObject instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) httpObject;
            if (location != null && location instanceof ProxyPassLocationConfig) {
                ProxyPassLocationConfig loc = (ProxyPassLocationConfig) location;
                tracer.info("Proxy.Request", passURI.getPath());
                if (loc.proxy_set_headers.size() > 0) {
                    tracer.debug("Proxy.Request.SetHeaders", "set headers=" + loc.proxy_set_headers);
                    for (Map.Entry<String, String> item : loc.proxy_set_headers.entrySet()) {
                        HttpHeaders.setHeader(request, item.getKey(), item.getValue());
                    }

                }
                request.setUri(passURI.toString());
            }
            if (location == null) {
                ByteBuf buffer = Unpooled.wrappedBuffer(NZXConstants.ANSWER_404.getBytes());
                response = new DefaultFullHttpResponse(request.getProtocolVersion(), HttpResponseStatus.NOT_FOUND, buffer);
                HttpHeaders.setContentLength(response, buffer.readableBytes());
                HttpHeaders.setHeader(response, HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
                HttpHeaders.setHeader(response, Names.CONNECTION, Values.CLOSE);
            }
        }
        if (location != null && location.post_processing_enable && httpObject instanceof FullHttpRequest) {
            FullHttpRequest req = (FullHttpRequest) httpObject;
            if (req.getMethod().equals(HttpMethod.POST)) {
                putToPostProcessor(httpObject);
            }
        }
        return response;
    }

    @Override
    public HttpObject serverToProxyResponse(HttpObject httpObject) {
        if (httpObject instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) httpObject;
            StringBuilder logLine = new StringBuilder();
            logLine.append("LEN=" + response.headers().get(HttpHeaders.Names.CONTENT_LENGTH));
            logLine.append(" ");
            logLine.append(response.getProtocolVersion().toString());
            logLine.append(" ");
            logLine.append("[" + response.getStatus() + "]");
            logLine.append(" ");

            logLine.append(response.getDecoderResult().toString());
            tracer.info("Server.Response", logLine.toString());
        }
        if (location != null && location.post_processing_enable && httpObject instanceof FullHttpResponse) {
            putToPostProcessor(httpObject);
        }
        return httpObject;
    }

    @Override
    public HttpObject proxyToClientResponse(HttpObject httpObject) {
        tracer.trace("Proxy.Response", httpObject.toString());
        return httpObject;
    }

    @Override
    public void serverToProxyResponseTimedOut() {
        super.serverToProxyResponseTimedOut();
        tracer.warn("Server.TimedOut/CONNECTION_ERROR", "");
    }

    @Override
    public void proxyToServerConnectionFailed() {
        super.proxyToServerConnectionFailed();
        tracer.warn("Server.ConnectionFailed/CONNECTION_ERROR", "");
    }

    protected static URI makePassURI(URI uri, ProxyPassLocationConfig loc) throws URISyntaxException {
        String path = uri.normalize().getPath();
        URI result = loc.proxy_pass;
        String pathTail = path.substring(loc.path.length());
        if (pathTail.length() > 0) {
            result = new URI(result.toString() + "/" + pathTail).normalize();
        }
        if (uri.getRawQuery() != null) {
            if (result.getPath().length() == 0) {
                result = new URI(result.toString() + "/");
            }
            result = new URI(result.toString() + "?" + uri.getRawQuery()).normalize();
        }
        return result;
    }

    protected void putToPostProcessor(HttpObject httpObject) {
        ByteBuf content = null;
        Tank.Type type = null;
        if (httpObject instanceof FullHttpRequest) {
            content = ((FullHttpRequest) httpObject).content();
            type = Type.REQ;
        }
        if (httpObject instanceof FullHttpResponse) {
            content = ((FullHttpResponse) httpObject).content();
            type = Type.RES;
        }

        if (content != null && content.isReadable()) {
            int rix = content.readerIndex();
            Tank tank = postProcessor.getTank();
            tank.type = type;
            tank.requestID = requestID;
            tank.requestDateTime = requestDateTime;
            tank.requestURI = requestURI;
            tank.properties.put(LocationConfig.DUMP_CONTENT_STORE, location.dump_content_store);
            tank.contentLength = content.readableBytes();
            content.readBytes(tank.data, 0, Math.min(content.readableBytes(), tank.data.length));
            postProcessor.put(tank);
            content.setIndex(rix, content.writerIndex());
        }
        postProcessor.lock.lock();
        try {
            postProcessor.check.signalAll();
        } finally {
            postProcessor.lock.unlock();
        }
    }

}
