package ru.otr.nzx.http.location;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import ru.otr.nzx.config.NZXConfigHelper;
import ru.otr.nzx.config.location.LocationConfig;
import ru.otr.nzx.config.location.ProxyPassLocationConfig;
import ru.otr.nzx.dumper.Dumper;
import ru.otr.nzx.dumper.Dumper.Tank;

public class LocationAdapter extends HttpFiltersAdapter {
    private static final DateFormat dayDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat idDateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss_SSS");

    private final Dumper dumper;
    private final Tracer tracer;

    private final URI uri;
    private final LocationConfig location;

    private final URI passURI;

    private final boolean dump_content_flag;
    private final String dump_content_dir;
    private final String dump_content_namePart;

    public LocationAdapter(HttpRequest originalRequest, ChannelHandlerContext ctx, Map<String, LocationConfig> locations, Dumper dumper, Tracer tracer) {
        super(originalRequest, ctx);
        String requestID = makeRequestID();
        Date requestDateTime = new Date();

        StringBuilder logLine = new StringBuilder();
        logLine.append("ID=" + requestID);
        logLine.append(" ");
        logLine.append("FROM " + ctx.channel().remoteAddress().toString());
        logLine.append(" ");
        logLine.append(originalRequest.getMethod().name());
        logLine.append(" ");
        logLine.append(originalRequest.getUri());
        logLine.append(" ");
        logLine.append(originalRequest.getProtocolVersion().toString());
        logLine.append(" ");
        logLine.append("LEN=" + originalRequest.headers().get(HttpHeaders.Names.CONTENT_LENGTH));
        logLine.append(" ");
        logLine.append(originalRequest.getDecoderResult().toString());
        tracer.info("Client.Request", logLine.toString());

        tracer.debug("Client.Request.GetHeaders", "ID=" + requestID + " headers=" + originalRequest.headers().entries());

        try {
            uri = new URI(originalRequest.getUri()).normalize();
            location = NZXConfigHelper.locate(uri.getPath(), locations);

            if (location != null && location instanceof ProxyPassLocationConfig) {
                this.dumper = dumper;
                this.tracer = tracer.getSubtracer("loc:" + location.path).getSubtracer(requestID);
                ProxyPassLocationConfig loc = (ProxyPassLocationConfig) location;
                this.passURI = makePassURI(uri, loc);
                this.tracer.info("Client.ProxyPass", passURI.toString());
                this.dump_content_flag = (loc.dump_content_enable && originalRequest.getMethod().equals(HttpMethod.POST));
                this.dump_content_dir = loc.dump_content_store + "/" + dayDateFormat.format(requestDateTime);
                this.dump_content_namePart = idDateFormat.format(requestDateTime) + "_" + requestID + "_" + uri.getPath().replaceAll("[^ \\w]", "_");
            } else {
                tracer.info("Client.UnresolvedRequest", "ID=" + requestID);
                this.dumper = null;
                this.tracer = tracer;
                this.passURI = null;
                this.dump_content_flag = false;
                this.dump_content_dir = null;
                this.dump_content_namePart = null;
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
                tracer.info("Proxy.Request", passURI.toString());
                if (loc.proxy_set_headers.size() > 0) {
                    for (Map.Entry<String, String> item : loc.proxy_set_headers.entrySet()) {
                        HttpHeaders.setHeader(request, item.getKey(), item.getValue());
                    }
                    tracer.debug("Proxy.Request.SetHeaders", "set headers=" + loc.proxy_set_headers);
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
        if (dump_content_flag && httpObject instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) httpObject;
            dump(dump_content_dir, dump_content_namePart + "_REQ", request.content());
        }
        return response;
    }

    @Override
    public HttpObject serverToProxyResponse(HttpObject httpObject) {
        if (httpObject instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) httpObject;
            StringBuilder logLine = new StringBuilder();
            logLine.append(response.getProtocolVersion().toString());
            logLine.append(" ");
            logLine.append("[" + response.getStatus() + "]");
            logLine.append(" ");
            logLine.append("LEN=" + response.headers().get(HttpHeaders.Names.CONTENT_LENGTH));
            logLine.append(" ");
            logLine.append(response.getDecoderResult().toString());
            tracer.info("Server.Response", logLine.toString());
        }
        if (dump_content_flag && httpObject instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) httpObject;
            dump(dump_content_dir, dump_content_namePart + "_RESP", response.content());
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
        tracer.warn("Server.TimedOut/NOTIFY_ADMIN", "");
    }

    @Override
    public void proxyToServerConnectionFailed() {
        super.proxyToServerConnectionFailed();
        tracer.warn("Server.ConnectionFailed/NOTIFY_ADMIN", "");
    }

    protected static String makeRequestID() {
        String result = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update((System.currentTimeMillis() + "-" + System.nanoTime()).getBytes());
            result = new BigInteger(1, md.digest()).toString(16);
            while (result.length() < 32) {
                result = "0" + result;
            }
            result = result.substring(0, 10);
        } catch (NoSuchAlgorithmException e) {
        }
        return result;
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

    protected void dump(String directoryPath, String fileName, ByteBuf content) {
        if (content.isReadable()) {
            int rix = content.readerIndex();
            Tank tank = dumper.getTank();
            tank.directoryPath = directoryPath;
            tank.fileName = fileName;
            tank.contentLength = content.readableBytes();
            content.readBytes(tank.data, 0, content.readableBytes());
            dumper.add(tank);
            content.setIndex(rix, content.writerIndex());
        }
        dumper.lock.lock();
        try {
            dumper.check.signalAll();
        } finally {
            dumper.lock.unlock();
        }
    }

}
