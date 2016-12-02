package ru.otr.nzx.config.http;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.otr.nzx.config.Config;

public class HTTPConfig extends Config {
    public final static String SERVERS = "servers";

    public final List<HTTPServerConfig> servers;

    public HTTPConfig(JSONObject src, String route, final Map<String, Object> routes) throws URISyntaxException {
        super(route, routes);
        servers = new ArrayList<>();
        routes.put(route + "/" + SERVERS, servers);
        JSONArray srvArray = src.getJSONArray(SERVERS);
        for (int i = 0; i < srvArray.length(); i++) {
            servers.add(new HTTPServerConfig(srvArray.getJSONObject(i), route + "/" + SERVERS, routes));
        }
    }

    @Override
    public JSONObject toJSON() {
        JSONObject http = new JSONObject();
        for (HTTPServerConfig item : servers) {
            http.append(SERVERS, item.toJSON());
        }
        return http;
    }
}
