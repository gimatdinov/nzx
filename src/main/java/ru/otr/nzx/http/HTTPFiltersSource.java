package ru.otr.nzx.http;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;

import cxc.jex.tracer.Tracer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import ru.otr.nzx.config.NZXConfigHelper;
import ru.otr.nzx.config.http.HTTPServerConfig;
import ru.otr.nzx.config.http.location.LocationConfig;
import ru.otr.nzx.http.HTTPServer.HttpObjectType;
import ru.otr.nzx.http.location.LocationAdapter;
import ru.otr.nzx.http.postprocessing.HTTPPostProcessor;

public class HTTPFiltersSource extends HttpFiltersSourceAdapter {

	private static MessageDigest md;
	
	private final HTTPServerConfig config;
	private final HTTPPostProcessor postProcessor;
	private final Tracer tracer;

	static {
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
		}
	}

	public HTTPFiltersSource(HTTPServerConfig config, HTTPPostProcessor postProcessor, Tracer tracer) {
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
		StringBuilder logLine = new StringBuilder();
		logLine.append("ID=" + requestID);
		logLine.append(" ");
		logLine.append(request.getUri());
		logLine.append(" ");
		logLine.append(HttpObjectType.REQ);
		logLine.append(" ");
		logLine.append("LEN=" + request.headers().get(HttpHeaders.Names.CONTENT_LENGTH));
		logLine.append(" ");
		logLine.append(request.getProtocolVersion().toString());
		logLine.append(" ");
		logLine.append(request.getMethod().name());
		logLine.append(" ");
		logLine.append("from " + ctx.channel().remoteAddress().toString());
		logLine.append(" ");
		logLine.append(request.getDecoderResult().toString());
		if (tracer.isDebugEnabled()) {
			logLine.append("\n");
			logLine.append("headers=" + request.headers().entries());
		}
		tracer.info("Filter.Request", logLine.toString());

		try {
			URI requestURI = new URI(request.getUri()).normalize();
			LocationConfig location = NZXConfigHelper.locate(requestURI.getPath(), config.locations);
			return new LocationAdapter(request, ctx, requestDateTime, requestID, requestURI, location, postProcessor, tracer);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private static String makeRequestID() {
		String result = null;
		md.reset();
		md.update((System.currentTimeMillis() + "-" + System.nanoTime()).getBytes());
		result = new BigInteger(1, md.digest()).toString(16);
		while (result.length() < 32) {
			result = "0" + result;
		}
		result = result.substring(0, 10);
		return result;
	}
}
