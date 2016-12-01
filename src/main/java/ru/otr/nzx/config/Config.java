package ru.otr.nzx.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.json.JSONObject;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import ru.otr.nzx.util.NZXUtil;

public abstract class Config {
    public abstract JSONObject toJSON();

    public Config(JSONObject src, String route, Map<String, Config> routes) throws URISyntaxException {
        new URI(route);
        if (routes.containsKey(route)) {
            throw new URISyntaxException(route, "Route already bound");
        }
        routes.put(route, this);
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }

    public String toString(int indentFactor) {
        return toJSON().toString(indentFactor);
    }

    public FullHttpResponse toHttpResponse() {
        return NZXUtil.makeSimpleResponse(this.toString(4), "application/json", 200, HttpVersion.HTTP_1_1);
    }
}
