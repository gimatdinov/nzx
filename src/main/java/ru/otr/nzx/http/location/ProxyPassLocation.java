package ru.otr.nzx.http.location;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Map;

import cxc.jex.postprocessing.PostProcessor;
import cxc.jex.tracer.Tracer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import ru.otr.nzx.config.http.location.ProxyPassLocationConfig;
import ru.otr.nzx.http.HTTPServer.ObjectType;

public class ProxyPassLocation extends Location {

    private final URI passURI;

    public ProxyPassLocation(HttpRequest originalRequest, ChannelHandlerContext ctx, Date requestDateTime, String requestID, URI requestURI,
            ProxyPassLocationConfig location, PostProcessor postProcessor, Tracer tracer) {
        super(originalRequest, ctx, requestDateTime, requestID, requestURI, location, postProcessor, tracer);

        try {
            this.passURI = makePassURI(requestURI, (ProxyPassLocationConfig) location);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
        tracer.info("Request", "Pass " + passURI.getPath());
        if (httpObject instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) httpObject;
            ProxyPassLocationConfig cfg = (ProxyPassLocationConfig) config;
            request.setUri(passURI.toString());

            if (cfg.proxy_set_headers.size() > 0) {
                tracer.debug("Server.Request.SetHeaders", "set headers=" + cfg.proxy_set_headers);
                for (Map.Entry<String, String> item : cfg.proxy_set_headers.entrySet()) {
                    HttpHeaders.setHeader(request, item.getKey(), item.getValue());
                }
            }

            if (config.post_processing_enable && request.getMethod().equals(HttpMethod.POST)) {
                putToPostProcessor(httpObject);
            }
        }
        return null;
    }

    @Override
    public HttpObject serverToProxyResponse(HttpObject httpObject) {
        if (httpObject instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) httpObject;
            StringBuilder logLine = new StringBuilder();
            logLine.append(ObjectType.RES);
            logLine.append("(");
            logLine.append(response.getStatus().code());
            logLine.append(") ");
            logLine.append("LEN=" + response.headers().get(HttpHeaders.Names.CONTENT_LENGTH));
            logLine.append(" ");
            logLine.append(response.getProtocolVersion().toString());
            logLine.append(" ");
            logLine.append(response.getDecoderResult().toString());


            tracer.info("Server.Response", logLine.toString());

            if (config.post_processing_enable) {
                putToPostProcessor(response);
            }
        }
        return httpObject;
    }

    @Override
    public HttpObject proxyToClientResponse(HttpObject httpObject) {
        if (tracer.isTraceEnabled()) {
            tracer.trace("Proxy.Response", httpObject.toString());
        }
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

    protected static URI makePassURI(URI uri, ProxyPassLocationConfig cfg) throws URISyntaxException {
        String path = uri.normalize().getPath();
        URI result = cfg.proxy_pass;
        String pathTail = path.substring(cfg.path.length());
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
