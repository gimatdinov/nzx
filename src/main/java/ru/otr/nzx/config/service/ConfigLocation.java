package ru.otr.nzx.config.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
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
import ru.otr.nzx.config.Config;
import ru.otr.nzx.config.http.location.HeadersConfigMap;
import ru.otr.nzx.config.http.location.LocationConfig;
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
            tracer.error("Make.ConfigURI.Error", e.getMessage(), e);
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
            Map<String, String> parameters = new HashMap<>();
            for (NameValuePair item : URLEncodedUtils.parse(cfgURI, "UTF-8")) {
                parameters.put(item.getName(), item.getValue());
            }
            if (node != null) {
                tracer.debug("Config.Request", request.getMethod() + " " + cfgURI.getPath() + " " + parameters.toString());
                try {
                    if (HttpMethod.PUT.equals(request.getMethod())) {
                        if (node instanceof LocationConfig) {
                            return configToHttpResponse(cfgService.updateLocation((LocationConfig) node, parameters));
                        }
                        if (node instanceof ActionConfig) {
                            return configToHttpResponse(cfgService.updateAction((ActionConfig) node, parameters));
                        }
                        if (node instanceof HeadersConfigMap) {
                            return configToHttpResponse(cfgService.updateLocationHeaders((HeadersConfigMap) node, parameters));
                        }
                    }
                    if (HttpMethod.POST.equals(request.getMethod())) {
                        if (node instanceof LocationConfigMap) {
                            return configToHttpResponse(cfgService.createLocation((LocationConfigMap) node, parameters));
                        }
                    }
                    if (HttpMethod.DELETE.equals(request.getMethod())) {
                        if (node instanceof LocationConfig) {
                            cfgService.deleteLocation((LocationConfig) node);
                            return NZXUtil.makeSimpleResponse("{\n    //Location deleted\n}", "application/json; charset=UTF-8", 200, HttpVersion.HTTP_1_1);
                        }
                        if (node instanceof HeadersConfigMap) {
                            return configToHttpResponse(cfgService.deleteLocationHeaders((HeadersConfigMap) node));
                        }
                    }
                    return configToHttpResponse(node);
                } catch (Exception e) {
                    tracer.error("Config.Request.Error", request.getMethod() + " " + cfgURI.getPath(), e);
                    return NZXUtil.makeSimpleResponse(e.getMessage(), "text/plain", 500, HttpVersion.HTTP_1_1);
                }
            }
        }
        return makeNotFoundResponse();
    }

    public static FullHttpResponse configToHttpResponse(Config cfg) {
        return NZXUtil.makeSimpleResponse(cfg.toString(4), "application/json; charset=UTF-8", 200, HttpVersion.HTTP_1_1);
    }

    private static FullHttpResponse makeNotFoundResponse() {
        return NZXUtil.makeSimpleResponse("{\n    //Not Found\n}", "application/json; charset=UTF-8", 404, HttpVersion.HTTP_1_1);
    }

}
