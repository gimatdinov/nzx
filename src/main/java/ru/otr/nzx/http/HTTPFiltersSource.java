package ru.otr.nzx.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.UUID;

import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;

import cxc.jex.postprocessing.PostProcessor;
import cxc.jex.tracer.Tracer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import ru.otr.nzx.config.http.HTTPServerConfig;
import ru.otr.nzx.config.http.location.*;
import ru.otr.nzx.http.location.*;
import ru.otr.nzx.util.NZXUtil;

public class HTTPFiltersSource extends HttpFiltersSourceAdapter {

	private final HTTPServerConfig config;
	private final PostProcessor postProcessor;
	private final Tracer tracer;

	public HTTPFiltersSource(HTTPServerConfig config, PostProcessor postProcessor, Tracer tracer) {
		this.config = config;
		this.postProcessor = postProcessor;
		this.tracer = tracer;
	}

	@Override
	public int getMaximumRequestBufferSizeInBytes() {
		return config.max_request_buffer_size;
	}

	@Override
	public int getMaximumResponseBufferSizeInBytes() {
		return config.max_response_buffer_size;
	}

	public HttpFilters filterRequest(HttpRequest request, ChannelHandlerContext ctx) {
		String requestID = makeRequestID();
		Date requestDateTime = new Date();
		tracer.info("Request", "ID=" + requestID + " " + NZXUtil.requestToLongLine(request, ctx, tracer.isDebugEnabled()));
		try {
			URI requestURI = new URI(request.getUri()).normalize();
			if (HttpMethod.GET.equals(request.getMethod()) || HttpMethod.POST.equals(request.getMethod())) {
				LocationConfig location = config.locate(requestURI.getPath());
				if (location != null && location.enable) {
					if (location instanceof ProxyPassLocationConfig) {
						return new ProxyPassLocation(request, ctx, requestDateTime, requestID, requestURI, (ProxyPassLocationConfig) location, postProcessor,
						        tracer.getSubtracer(location.path));
					}
					return new FailureLocation(request, ctx, requestDateTime, requestID, requestURI, null, postProcessor, tracer.getSubtracer(location.path),
					        500);
				} else {
					return new FailureLocation(request, ctx, requestDateTime, requestID, requestURI, null, postProcessor,
					        tracer.getSubtracer("#NotFound"), 404);
				}
			} else {
				return new FailureLocation(request, ctx, requestDateTime, requestID, requestURI, null, postProcessor, tracer.getSubtracer("#MethodNotAllowed"), 405);
			}
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private static String makeRequestID() {
		return UUID.randomUUID().toString().substring(26, 35);
	}
	
}
