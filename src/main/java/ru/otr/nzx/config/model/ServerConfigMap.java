package ru.otr.nzx.config.model;

import java.net.URISyntaxException;

import org.json.JSONArray;
import org.json.JSONObject;

public class ServerConfigMap extends ConfigMap<ServerConfig> {

    public ServerConfigMap(JSONArray src, String name, Config host) throws URISyntaxException {
        super(src, name, host);
    }

    @Override
    protected ServerConfig makeItem(JSONObject src) throws URISyntaxException {
        return new ServerConfig(src, this);
    }

}
