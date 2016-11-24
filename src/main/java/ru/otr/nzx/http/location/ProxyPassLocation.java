package ru.otr.nzx.http.location;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

import cxc.jex.postprocessing.PostProcessor;
import cxc.jex.tracer.Tracer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import ru.otr.nzx.config.http.location.ProxyPassLocationConfig;
import ru.otr.nzx.https.MITM;
import ru.otr.nzx.util.NZXUtil;

public class ProxyPassLocation extends Location {
    private static final Pattern HTTPS_SCHEME = Pattern.compile("^https://.*", Pattern.CASE_INSENSITIVE);

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
        HttpResponse response = null;
        tracer.info("Client.Request", "PASS " + passURI.toString());
        if (httpObject instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) httpObject;
            request.setUri(passURI.toString());
            ProxyPassLocationConfig cfg = (ProxyPassLocationConfig) config;
            if (cfg.proxy_set_headers.size() > 0) {
                tracer.debug("Proxy.Request.SetHeaders", "headers=" + cfg.proxy_set_headers);
                for (Map.Entry<String, String> item : cfg.proxy_set_headers.entrySet()) {
                    HttpHeaders.setHeader(request, item.getKey(), item.getValue());
                }
            }
            if (config.post_processing_enable) {
                putToPostProcessor(httpObject);
            }
            if (HTTPS_SCHEME.matcher(passURI.toString()).matches()) {
                try {
                    response = new MITM().sendRequest(request);
                    tracer.info("Server.Response", "MITM " + NZXUtil.responseToShortLine(response));
                    if (config.post_processing_enable) {
                        putToPostProcessor(response);
                    }
                } catch (Exception e) {
                    response = NZXUtil.makeFailureResponse(500, request.getProtocolVersion());
                    tracer.error("Server.Connection.Failed/CONNECTION_ERROR", "MITM", e);
                }

            }

        }
        return response;
    }

    @Override
    public HttpResponse proxyToServerRequest(HttpObject httpObject) {
        tracer.debug("Proxy.Request", "");
        return null;
    }

    @Override
    public HttpObject serverToProxyResponse(HttpObject httpObject) {
        if (httpObject instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) httpObject;
            tracer.info("Server.Response", NZXUtil.responseToShortLine(response));

            if (config.post_processing_enable) {
                putToPostProcessor(response);
            }
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
        tracer.warn("Server.Response.TimedOut/CONNECTION_ERROR", "");
    }

    @Override
    public void proxyToServerConnectionFailed() {
        super.proxyToServerConnectionFailed();
        tracer.warn("Server.Connection.Failed/CONNECTION_ERROR", "");
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
