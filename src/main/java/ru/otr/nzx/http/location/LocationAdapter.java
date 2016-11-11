package ru.otr.nzx.http.location;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

public class LocationAdapter extends HttpFiltersAdapter {
    private static final DateFormat idDateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss_SSS");

    private final Tracer tracer;

    private final URI uri;
    private final LocationConfig location;

    private final URI passURI;
    private final boolean dump_body_flag;
    private final String bodyDumpPathPart;

    public LocationAdapter(HttpRequest originalRequest, ChannelHandlerContext ctx, Map<String, LocationConfig> locations, Tracer tracer) {
        super(originalRequest, ctx);
        String requestID = makeRequestID();

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
        tracer.trace("Client.Request", logLine.toString());

        tracer.trace("Client.Request.GetHeaders/DEBUG", "ID=" + requestID + " headers=" + originalRequest.headers().entries());

        try {
            uri = new URI(originalRequest.getUri()).normalize();
            location = NZXConfigHelper.locate(uri.getPath(), locations);

            if (location != null && location instanceof ProxyPassLocationConfig) {
                this.tracer = tracer.getSubtracer(location.path).getSubtracer(requestID);
                ProxyPassLocationConfig loc = (ProxyPassLocationConfig) location;
                this.passURI = makePassURI(uri, loc);
                this.tracer.trace("Client.ProxyPass", passURI.toString());
                this.dump_body_flag = (loc.dump_body_POST && originalRequest.getMethod().equals(HttpMethod.POST));
                this.bodyDumpPathPart = loc.dump_body_store + "/" + idDateFormat.format(new Date()) + "_" + requestID + "_"
                        + uri.getPath().replaceAll("[^ \\w]", "_");
            } else {
                tracer.trace("Client.UnresolvedRequest", "ID=" + requestID);
                this.tracer = tracer;
                this.passURI = null;
                this.dump_body_flag = false;
                this.bodyDumpPathPart = null;
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
                tracer.trace("Proxy.Request", passURI.toString());
                if (loc.proxy_set_headers.size() > 0) {
                    for (Map.Entry<String, String> item : loc.proxy_set_headers.entrySet()) {
                        HttpHeaders.setHeader(request, item.getKey(), item.getValue());
                    }
                    tracer.trace("Proxy.Request.SetHeaders/DEBUG", "set headers=" + loc.proxy_set_headers);
                }
                request.setUri(passURI.toString());
            }
            if (location == null) {
                ByteBuf buffer = Unpooled.wrappedBuffer(NZXConstants.ANSWER_404.getBytes());
                response = new DefaultFullHttpResponse(request.getProtocolVersion(), HttpResponseStatus.NOT_FOUND, buffer);
                HttpHeaders.setContentLength(response, buffer.readableBytes());
                HttpHeaders.setHeader(response, HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
                HttpHeaders.setHeader(response, Names.CONNECTION, Values.CLOSE);
                tracer.trace("Proxy.Response.Answer404", response.getProtocolVersion() + " [" + response.getStatus() + "]");
            }
        }
        if (dump_body_flag && httpObject instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) httpObject;
            tracer.trace("Dump.ClientRequest.Start", "LEN=" + request.headers().get(HttpHeaders.Names.CONTENT_LENGTH));
            File dump = new File(bodyDumpPathPart + "_REQ");
            try (FileOutputStream dumpFOS = new FileOutputStream(dump)) {
                int rix = request.content().readerIndex();
                while (request.content().isReadable()) {
                    dumpFOS.write(request.content().readByte());
                }
                request.content().setIndex(rix, request.content().writerIndex());
                tracer.trace("Dump.ClientRequest.Stop", "file=" + dump.toPath());
            } catch (IOException e) {
                tracer.trace("Dump.ClientRequest.Error/ERROR", e.getMessage(), e);
            }
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
            tracer.trace("Server.Response", logLine.toString());
        }
        if (dump_body_flag && httpObject instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) httpObject;
            tracer.trace("Dump.ServerRequest.Start", "LEN=" + response.headers().get(HttpHeaders.Names.CONTENT_LENGTH));
            File dump = new File(bodyDumpPathPart + "_RESP");

            try (FileOutputStream dumpFOS = new FileOutputStream(dump)) {
                int rix = response.content().readerIndex();
                while (response.content().isReadable()) {
                    dumpFOS.write(response.content().readByte());
                }
                response.content().setIndex(rix, response.content().writerIndex());
                tracer.trace("Dump.ServerRequest.Stop", "file=" + dump.toPath());
            } catch (IOException e) {
                tracer.trace("Dump.ServerRequest.Error/ERROR", e.getMessage(), e);
            }
        }
        return httpObject;

    }

    // @Override
    // public HttpObject proxyToClientResponse(HttpObject httpObject) {
    // HttpObject result = httpObject;
    // if (httpObject instanceof HttpResponse) {
    // HttpResponse response = (HttpResponse) httpObject;
    // if (response.getStatus().code() >= 500) {
    // ByteBuf buffer =
    // Unpooled.wrappedBuffer(NZXConstants.ANSWER_500.getBytes());
    // response = new DefaultFullHttpResponse(response.getProtocolVersion(),
    // HttpResponseStatus.INTERNAL_SERVER_ERROR, buffer);
    // HttpHeaders.setContentLength(response, buffer.readableBytes());
    // HttpHeaders.setHeader(response, HttpHeaders.Names.CONTENT_TYPE,
    // "text/html; charset=UTF-8");
    // result = response;
    // }
    // }
    // log.warn(requestID + " RESP: proxy " + result);
    // return result;
    // }

    @Override
    public void serverToProxyResponseTimedOut() {
        super.serverToProxyResponseTimedOut();
        tracer.trace("Server.TimedOut/ERROR", "");
    }

    @Override
    public void proxyToServerConnectionFailed() {
        super.proxyToServerConnectionFailed();
        tracer.trace("Server.ConnectionFailed/ERROR", "");
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

}
