package ru.otr.nzx.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.littleshoot.proxy.HttpFiltersAdapter;

import cxc.jex.tracer.Tracer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import ru.otr.nzx.config.http.location.HeadersConfigMap;
import ru.otr.nzx.config.http.location.LocationConfig;
import ru.otr.nzx.config.http.location.LocationConfig.LocationType;
import ru.otr.nzx.config.http.location.LocationConfigMap;
import ru.otr.nzx.config.postprocessing.ActionConfig;
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
            Config node = cfgService.getContext().get(cfgURI.getPath());
            if (node != null) {
                tracer.trace(request.getMethod().toString(), cfgURI.getPath());
                if (HttpMethod.PUT.equals(request.getMethod())) {
                    try {
                        if (node instanceof LocationConfig) {
                            return put((LocationConfig) node, URLEncodedUtils.parse(cfgURI, "UTF-8"));
                        }
                        if (node instanceof ActionConfig) {
                            return put((ActionConfig) node, URLEncodedUtils.parse(cfgURI, "UTF-8"));
                        }
                        if (node instanceof LocationConfigMap) {
                            return put((LocationConfigMap) node, URLEncodedUtils.parse(cfgURI, "UTF-8"));
                        }
                        if (node instanceof HeadersConfigMap) {
                            return put((HeadersConfigMap) node, URLEncodedUtils.parse(cfgURI, "UTF-8"));
                        }
                    } catch (Exception e) {
                        return NZXUtil.makeSimpleResponse(e.getMessage(), "text/plain", 400, HttpVersion.HTTP_1_1);
                    }
                }
                if (HttpMethod.DELETE.equals(request.getMethod())) {
                    try {
                        if (node instanceof LocationConfig) {
                            return delete((LocationConfig) node);
                        }
                        if (node instanceof HeadersConfigMap) {
                            return delete((HeadersConfigMap) node);
                        }
                    } catch (Exception e) {
                        return NZXUtil.makeSimpleResponse(e.getMessage(), "text/plain", 400, HttpVersion.HTTP_1_1);
                    }
                }
                return NZXUtil.configToHttpResponse(node);
            }
        }
        return NZXUtil.makeSimpleResponse("non existent", "text/plain", 404, HttpVersion.HTTP_1_1);
    }

    private FullHttpResponse put(LocationConfig node, List<NameValuePair> params) throws URISyntaxException {
        synchronized (node) {
            for (NameValuePair item : params) {
                if (item.getName().equals(LocationConfig.ENABLE)) {
                    node.enable = Boolean.valueOf(item.getValue());
                }
                if (node.type == LocationType.FILE) {
                    break;
                }
                if (item.getName().equals(LocationConfig.PROXY_PASS)) {
                    node.proxy_pass = new URI(item.getValue());
                    node.type = LocationType.PROXY_PASS;
                }
            }
            return NZXUtil.configToHttpResponse(node);
        }
    }

    private FullHttpResponse put(ActionConfig node, List<NameValuePair> params) throws URISyntaxException {
        synchronized (node) {
            Map<String, String> parameters = new HashMap<>();
            for (NameValuePair item : params) {
                parameters.put(item.getName(), item.getValue());
            }
            node.setParameters(parameters);
            return NZXUtil.configToHttpResponse(node);
        }
    }

    private FullHttpResponse put(LocationConfigMap node, List<NameValuePair> params) throws URISyntaxException {
        synchronized (node) {
            String path = null;
            for (NameValuePair item : params) {
                if (item.getName().equals(LocationConfig.PATH)) {
                    path = item.getValue();
                }
            }
            if (path == null || path.length() == 0) {
                throw new IllegalArgumentException("path cannot be empty!");
            }
            new LocationConfig(path, node);
            return NZXUtil.configToHttpResponse(node);
        }
    }

    private HttpResponse put(HeadersConfigMap node, List<NameValuePair> params) {
        synchronized (node) {
            for (NameValuePair item : params) {
                node.put(item.getName(), item.getValue());
            }
            return NZXUtil.configToHttpResponse(node);
        }
    }

    private FullHttpResponse delete(LocationConfig node) {
        node.delete();
        return NZXUtil.makeSimpleResponse("location deleted", "text/plain", 200, HttpVersion.HTTP_1_1);
    }

    private HttpResponse delete(HeadersConfigMap node) {
        node.clear();
        return NZXUtil.makeSimpleResponse("headers deleted", "text/plain", 200, HttpVersion.HTTP_1_1);
    }
}
