package ru.otr.nzx.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpHeaders.Values;
import ru.otr.nzx.NZXConstants;
import ru.otr.nzx.http.HTTPServer.ObjectType;
import ru.otr.nzx.postprocessing.NZXTank;

public class NZXUtil {
	private static final DateFormat idDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	public static String requestToLongLine(HttpRequest request, ChannelHandlerContext ctx, boolean debug) {
		StringBuilder result = new StringBuilder();
		result.append(request.getUri());
		result.append(" ");
		result.append(ObjectType.REQ);
		result.append(" ");
		result.append("LEN=" + request.headers().get(HttpHeaders.Names.CONTENT_LENGTH));
		result.append(" ");
		result.append(request.getProtocolVersion().toString());
		result.append(" ");
		result.append(request.getMethod().name());
		if (ctx != null) {
			result.append(" ");
			result.append("from " + ctx.channel().remoteAddress().toString());
		}
		result.append(" ");
		result.append(request.getDecoderResult().toString());
		if (debug) {
			result.append("\n");
			result.append("headers=" + request.headers().entries());
		}
		return result.toString();
	}

	public static String responseToShortLine(HttpResponse response) {
		StringBuilder result = new StringBuilder();
		result.append(ObjectType.RES);
		result.append("(");
		result.append(response.getStatus().code());
		result.append(") ");
		result.append("LEN=" + response.headers().get(HttpHeaders.Names.CONTENT_LENGTH));
		result.append(" ");
		result.append(response.getProtocolVersion().toString());
		result.append(" ");
		result.append(response.getDecoderResult().toString());
		return result.toString();
	}

	public static String tankToShortLine(NZXTank tank) {
		StringBuilder result = new StringBuilder();
		result.append(idDateFormat.format(tank.requestDateTime));
		result.append(" ");
		result.append(tank.requestID);
		result.append(" ");
		result.append(tank.requestURI.getPath());
		result.append(" ");
		result.append(tank.type);
		if (tank.type == ObjectType.RES) {
			result.append("(");
			result.append(tank.responseStatusCode);
			result.append(")");
		}
		result.append(" ");
		result.append("LEN=");
		result.append(tank.contentLength);
		result.append(" ");
		result.append(tank.success ? "success" : "unfinished");
		return result.toString();
	}

	public static FullHttpResponse makeFailureResponse(int responseStatusCode, HttpVersion httpVersion) {
		String content = "<html><head><title>" + HttpResponseStatus.valueOf(responseStatusCode).toString()
		        + "</title></head><body bgcolor=\"white\"><center><h1>" + HttpResponseStatus.valueOf(responseStatusCode).toString()
		        + "</h1></center><hr><center>NZX " + NZXConstants.NZX_VERSION + "</center></body></html>";
		ByteBuf buffer = Unpooled.wrappedBuffer(content.getBytes());
		FullHttpResponse response = new DefaultFullHttpResponse(httpVersion, HttpResponseStatus.valueOf(responseStatusCode), buffer);
		HttpHeaders.setContentLength(response, buffer.readableBytes());
		HttpHeaders.setHeader(response, HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
		HttpHeaders.setHeader(response, Names.CONNECTION, Values.CLOSE);
		return response;
	}
}
