package ru.otr.nzx.https;

import java.util.Map.Entry;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
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
import io.netty.handler.codec.http.HttpRequest;

public class MITM {

    public MITM() {
    }

    public FullHttpResponse sendRequest(HttpRequest request) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse res = null;
        HttpRequestBase req = null;
        try {
            switch (request.getMethod().name()) {
            case "OPTIONS":
                req = new HttpOptions(request.getUri());
                break;
            case "HEAD":
                req = new HttpHead(request.getUri());
                break;
            case "GET":
                req = new HttpGet(request.getUri());
                break;
            case "POST":
                HttpPost postMsg = new HttpPost(request.getUri());
                FullHttpRequest postRequest = (FullHttpRequest) request;
                byte[] content = new byte[postRequest.content().readableBytes()];
                postRequest.content().readBytes(content);
                postMsg.setEntity(new ByteArrayEntity(content));
                req = postMsg;
                break;
            case "PUT":
                req = new HttpPut(request.getUri());
                break;
            case "PATCH":
                req = new HttpPatch(request.getUri());
                break;
            case "DELETE":
                req = new HttpDelete(request.getUri());
                break;
            case "TRACE":
                req = new HttpTrace(request.getUri());
                break;
            default:
                throw new Exception("MethodNotAllowed");
            }
            for (Entry<String, String> item : request.headers().entries()) {
                if (!item.getKey().equals(HTTP.CONTENT_LEN) && !item.getKey().equals(HTTP.TARGET_HOST)) {
                    req.setHeader(item.getKey(), item.getValue());
                }
            }
            res = httpclient.execute(req);
            ByteBuf buffer = Unpooled.buffer(0);
            if (res.getEntity() != null && res.getEntity().getContent() != null) {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    baos.write(res.getEntity().getContent());
                    buffer = Unpooled.wrappedBuffer(baos.toByteArray());
                } finally {
                }
            }
            FullHttpResponse response = new DefaultFullHttpResponse(request.getProtocolVersion(),
                    HttpResponseStatus.valueOf(res.getStatusLine().getStatusCode()), buffer);
            HttpHeaders.setContentLength(response, buffer.readableBytes());
            if (res.getEntity() != null && res.getEntity().getContentType() != null) {
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
