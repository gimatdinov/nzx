package ru.otr.nzx.extra.dumpsearch;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import cxc.jex.tracer.Tracer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import ru.otr.nzx.config.http.location.LocationConfig;
import ru.otr.nzx.http.location.Location;
import ru.otr.nzx.postprocessing.NZXPostProcessor;
import ru.otr.nzx.util.NZXUtil;

public class DumpSearchLocation extends Location {
    static final String CONST_QUERY_TEXT = "text";
    static final String CONST_HTML_PAGE = "<!DOCTYPE html><html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/><title>NZX: Search in dumps</title></head><body>%1s</body></html>";

    private final DumpSearchProcessor processor;

    public DumpSearchLocation(DumpSearchProcessor processor, HttpRequest originalRequest, ChannelHandlerContext ctx, Date requestDateTime, String requestID,
            URI requestURI, LocationConfig config, NZXPostProcessor postProcessor, Tracer tracer) {
        super(originalRequest, ctx, requestDateTime, requestID, requestURI, config, postProcessor, tracer);
        this.processor = processor;
    }

    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
        String body = "<p>Use search request format: " + config.path + "?" + CONST_QUERY_TEXT + "=<b><i>QUERY</i></b><p>";
        if (httpObject instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) httpObject;
            Map<String, String> parameters = new HashMap<>();
            for (NameValuePair item : URLEncodedUtils.parse(requestURI, "UTF-8")) {
                parameters.put(item.getName(), item.getValue());
            }
            try {
                String queryText = parameters.get(CONST_QUERY_TEXT);
                if (queryText != null && queryText.length() > 0) {
                    List<String> list = processor.search(queryText);
                    if (list.size() > 0) {
                        StringBuilder bodyBuilder = new StringBuilder();
                        bodyBuilder.append("<ol><samp>");
                        for (String item : list) {
                            bodyBuilder.append(String.format("<li><a href=\"%1s\" target=\"_blank\">%2s</a></li>", item, item));
                        }
                        bodyBuilder.append("</samp></ol>");
                        body = bodyBuilder.toString();
                    } else {
                        body = "<b>Not found.</b>";
                    }
                }
            } catch (IOException e) {
                NZXUtil.makeFailureResponse(500, request.getProtocolVersion());
            }
        }
        String content = String.format(CONST_HTML_PAGE, body);
        return NZXUtil.makeSimpleResponse(content, "text/html", 200, originalRequest.getProtocolVersion());
    }

}
