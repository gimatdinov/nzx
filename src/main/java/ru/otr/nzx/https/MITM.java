package ru.otr.nzx.https;

import java.util.Map.Entry;

import org.apache.commons.io.output.ByteArrayOutputStream;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpHeaders.Values;

public class MITM {

    public MITM() {
    }

    public FullHttpResponse sendRequest(FullHttpRequest request) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse res = null;
        HttpRequestBase req = null;
        try {
            switch (request.getMethod().name()) {
            case "GET":
                req = new HttpGet(request.getUri());
                break;
            case "POST":
                req = new HttpPost(request.getUri());
                break;
            default:
                throw new Exception("UnsupportedHttpMethod:" + request.getMethod().name());
            }
            for (Entry<String, String> item : request.headers().entries()) {
                req.setHeader(item.getKey(), item.getValue());
            }
            res = httpclient.execute(req);
            ByteBuf buffer = Unpooled.buffer(0);
            if (res.getEntity() != null) {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    baos.write(res.getEntity().getContent());
                    buffer = Unpooled.wrappedBuffer(baos.toByteArray());
                } finally {
                }
            }
            FullHttpResponse response = new DefaultFullHttpResponse(request.getProtocolVersion(),
                    HttpResponseStatus.valueOf(res.getStatusLine().getStatusCode()), buffer);
            HttpHeaders.setContentLength(response, buffer.readableBytes());
            if (res.getEntity() != null) {
                HttpHeaders.setHeader(response, res.getEntity().getContentType().getName(), res.getEntity().getContentType().getValue());
            }
            HttpHeaders.setHeader(response, Names.CONNECTION, Values.CLOSE);
            EntityUtils.consume(res.getEntity());
            return response;
        } finally {
            if (res != null) {
                res.close();
            }
            httpclient.close();
        }

    }

}
