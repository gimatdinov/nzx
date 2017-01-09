package ru.otr.nzx.extra.location;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Date;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import cxc.jex.tracer.Tracer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import ru.otr.nzx.config.model.LocationConfig;
import ru.otr.nzx.http.postprocessing.NZXPostProcessor;
import ru.otr.nzx.http.server.Location;
import ru.otr.nzx.util.NZXUtil;

public class GetIP extends Location {
    public final static String HOSTNAME = "hostname";

    public GetIP(HttpRequest originalRequest, ChannelHandlerContext ctx, Date requestDateTime, String requestID, URI requestURI,
            LocationConfig config, NZXPostProcessor postProcessor, Tracer tracer) {
        super(originalRequest, ctx, requestDateTime, requestID, requestURI, config, postProcessor, tracer);
    }

    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
        String hostname = null;
        for (NameValuePair item : URLEncodedUtils.parse(requestURI, "UTF-8")) {
            if (HOSTNAME.equals(item.getName())) {
                hostname = item.getValue();
                break;
            }
        }
        if (hostname != null) {
            try {
                InetAddress address = InetAddress.getByName(hostname);
                return NZXUtil.makeSimpleResponse(hostname + ": " + address.getHostAddress(), "text/plain", 200, originalRequest.getProtocolVersion());
            } catch (UnknownHostException e) {
                return NZXUtil.makeSimpleResponse(hostname + ": unknown host", "text/plain", 200, originalRequest.getProtocolVersion());
            }
        } else {
            return NZXUtil.makeSimpleResponse("Request: " + config.path + "?" + HOSTNAME + "=HOSTNAME", "text/plain", 200,
                    originalRequest.getProtocolVersion());
        }
    }

}
