package ru.otr.nzx.http.location;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Date;

import org.apache.commons.io.IOUtils;

import cxc.jex.tracer.Tracer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpHeaders.Values;
import ru.otr.nzx.config.http.location.LocationConfig;
import ru.otr.nzx.http.postprocessing.HTTPPostProcessor;
import ru.otr.nzx.util.NZXUtil;

public class FileLocation extends Location {

    public FileLocation(HttpRequest originalRequest, ChannelHandlerContext ctx, Date requestDateTime, String requestID, URI requestURI, LocationConfig config,
            HTTPPostProcessor postProcessor, Tracer tracer) {
        super(originalRequest, ctx, requestDateTime, requestID, requestURI, config, postProcessor, tracer);
    }

    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
        File file = new File(config.file).getAbsoluteFile();
        tracer.info("File", file.getPath());

        if (httpObject instanceof HttpRequest) {
            if (config.post_processing_enable) {
                putToPostProcessor(httpObject);
            }
        }

        FileInputStream fis = null;
        ByteBufOutputStream bbos = null;
        try {
            fis = new FileInputStream(file);
            ByteBuf buffer = Unpooled.buffer(fis.available());
            bbos = new ByteBufOutputStream(buffer);
            IOUtils.copy(fis, bbos);
            FullHttpResponse response = new DefaultFullHttpResponse(originalRequest.getProtocolVersion(), HttpResponseStatus.OK, buffer);
            HttpHeaders.setContentLength(response, buffer.readableBytes());
            HttpHeaders.setHeader(response, HttpHeaders.Names.CONTENT_TYPE, config.mimeType);
            HttpHeaders.setHeader(response, Names.CONNECTION, Values.CLOSE);
            return response;
        } catch (FileNotFoundException e) {
            tracer.warn("File.NotFound", file.getPath());
            return NZXUtil.makeFailureResponse(404, originalRequest.getProtocolVersion());
        } catch (IOException e) {
            tracer.warn("File.Error/NOTIFY_ADMIN", file.getPath(), e);
            return NZXUtil.makeFailureResponse(500, originalRequest.getProtocolVersion());
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (bbos != null) {
                    bbos.close();
                }
            } catch (IOException e) {

            }
        }

    }

}
