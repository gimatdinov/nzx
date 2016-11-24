package ru.otr.nzx.test;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestHttpMethods {
    private final static Logger log = LoggerFactory.getLogger(TestHttpMethods.class);
            
    public static void main(String... args) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse res = null;
        HttpRequestBase req1 = new HttpGet("http://localhost:8011/mitm");
        //HttpRequestBase req2 = new HttpOptions("https://yandex.ru");
        try {
            res = httpclient.execute(req1);
            log.info(res.toString());
            //res = httpclient.execute(req2);
            //log.info(res.toString());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
