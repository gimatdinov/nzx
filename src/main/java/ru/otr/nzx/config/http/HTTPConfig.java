package ru.otr.nzx.config.http;

import java.net.URISyntaxException;

import org.json.JSONObject;

import ru.otr.nzx.config.Config;

public class HTTPConfig extends Config {
    public final static String SERVERS = "servers";

    public final HTTPServerConfigMap servers;

    public HTTPConfig(JSONObject src, String name, Config host) throws URISyntaxException {
        super(name, host);
        servers = new HTTPServerConfigMap(src.getJSONArray(SERVERS), SERVERS, this);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject http = new JSONObject();
        http.put(SERVERS, servers.toJSON());
        return http;
    }
}
