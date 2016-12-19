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
import ru.otr.nzx.config.http.location.LocationConfig;
import ru.otr.nzx.http.location.Location;
import ru.otr.nzx.postprocessing.NZXPostProcessor;
import ru.otr.nzx.util.NZXUtil;

public class DumpSearchLocation extends Location {
	static final String CONST_QUERY_TEXT = "text";
	static final String CONST_SEARCH_FORM = "<form action=\"%1s\"><p><input type=\"search\" name=\"text\" %2s size=\"100\" placeholder=\"Search in dumps\"><input type=\"submit\" value=\"Search\"></p></form>";
	static final String CONST_HTML_PAGE = "<!DOCTYPE html><html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/><title>NZX: Search in dumps</title></head><body>"
	        + CONST_SEARCH_FORM + "<hr>%3s</body></html>";

	private final DumpSearchProcessor processor;

	public DumpSearchLocation(DumpSearchProcessor processor, HttpRequest originalRequest, ChannelHandlerContext ctx, Date requestDateTime, String requestID,
	        URI requestURI, LocationConfig config, NZXPostProcessor postProcessor, Tracer tracer) {
		super(originalRequest, ctx, requestDateTime, requestID, requestURI, config, postProcessor, tracer);
		this.processor = processor;
	}

	@Override
	public HttpResponse clientToProxyRequest(HttpObject httpObject) {
		String queryText = "";
		String body = "";
		if (httpObject instanceof HttpRequest) {
			HttpRequest request = (HttpRequest) httpObject;
			Map<String, String> parameters = new HashMap<>();
			for (NameValuePair item : URLEncodedUtils.parse(requestURI, "UTF-8")) {
				parameters.put(item.getName(), item.getValue());
			}
			try {
				queryText = parameters.get(CONST_QUERY_TEXT);
				if (queryText != null && queryText.length() > 0) {
					tracer.debug("Search.Request", request.getMethod() + " " + requestURI.getPath() + " " + parameters.toString());
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
				} else {
					queryText = "";
				}
			} catch (IOException e) {
				NZXUtil.makeFailureResponse(500, request.getProtocolVersion());
			}
		}
		String valueAttr = (queryText.length() > 0) ? "value=\"" + queryText + "\"" : "";
		String content = String.format(CONST_HTML_PAGE, config.path, valueAttr, body);
		return NZXUtil.makeSimpleResponse(content, "text/html", 200, originalRequest.getProtocolVersion());
	}

}
