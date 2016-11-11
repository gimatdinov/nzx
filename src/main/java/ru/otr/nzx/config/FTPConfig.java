package ru.otr.nzx.config;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class FTPConfig {
    public final static String SERVERS = "servers";

    public final List<FTPServerConfig> servers;

    public FTPConfig(JSONObject src) throws URISyntaxException {
        servers = new ArrayList<>();
        JSONArray srvArray = src.getJSONArray(SERVERS);
        for (int i = 0; i < srvArray.length(); i++) {
            servers.add(new FTPServerConfig(srvArray.getJSONObject(i)));

        }
    }

    public JSONObject toJSON() {
        JSONObject http = new JSONObject();
        for (FTPServerConfig item : servers) {
            http.append(SERVERS, item.toJSON());
        }
        return http;
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }
}
