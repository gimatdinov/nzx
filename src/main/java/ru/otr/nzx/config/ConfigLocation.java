package ru.otr.nzx.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.littleshoot.proxy.HttpFiltersAdapter;

import cxc.jex.tracer.Tracer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import ru.otr.nzx.config.http.location.ProxyPassLocationConfig;
import ru.otr.nzx.util.NZXUtil;

public class ConfigLocation extends HttpFiltersAdapter {
    private final Tracer tracer;
    private final NZXConfigService cfgService;
    private URI cfgURI;

    public ConfigLocation(HttpRequest originalRequest, ChannelHandlerContext ctx, Date requestDateTime, String requestID, NZXConfigService cfgService,
            Tracer tracer) {
        super(originalRequest, ctx);
        this.cfgService = cfgService;
        this.tracer = tracer;
        try {
            URI uri = new URI(originalRequest.getUri()).normalize();
            cfgURI = new URI(uri.getPath());
            if (uri.getRawQuery() != null) {
                cfgURI = new URI(cfgURI.toString() + "?" + uri.getRawQuery()).normalize();
            }
        } catch (URISyntaxException e) {
            tracer.error("Make.ConfigURI", e.getMessage(), e);
            cfgURI = null;
        }
    }

    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
        if (cfgURI == null) {
            return NZXUtil.makeFailureResponse(500, HttpVersion.HTTP_1_1);
        }
        if (httpObject instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) httpObject;
            if (HttpMethod.GET.equals(request.getMethod())) {
                tracer.info("GET", cfgURI.getPath());
                Config node = cfgService.getNode(cfgURI.getPath());
                if (node != null) {
                    return node.toHttpResponse();
                }
            }
            if (HttpMethod.PUT.equals(request.getMethod())) {
                tracer.info("GET", cfgURI.toString());
                Config node = cfgService.getNode(cfgURI.getPath());
                if (node != null) {
                    try {
                        if (node instanceof ProxyPassLocationConfig) {
                            update((ProxyPassLocationConfig) node, URLEncodedUtils.parse(cfgURI, "UTF-8"));
                        }
                    } catch (Exception e) {
                        return NZXUtil.makeSimpleResponse(e.getMessage(), "text/plain", 400, HttpVersion.HTTP_1_1);
                    }
                    return node.toHttpResponse();
                }
            }
        }
        return NZXUtil.makeSimpleResponse("non existent", "text/plain", 404, HttpVersion.HTTP_1_1);
    }

    private ProxyPassLocationConfig update(ProxyPassLocationConfig node, List<NameValuePair> params) throws URISyntaxException {
        synchronized (node) {
            for (NameValuePair item : params) {
                if (item.getName().equals(ProxyPassLocationConfig.PROXY_PASS)) {
                    node.setProxyPass(item.getValue());
                }
            }
        }
        return node;
    }
}
