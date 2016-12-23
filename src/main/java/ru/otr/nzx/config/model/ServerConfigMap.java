package ru.otr.nzx.config.model;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.otr.nzx.config.service.ConfigException;

public class ServerConfigMap extends ConfigMap<ServerConfig> {

    public ServerConfigMap(JSONArray src, String name, Config host) throws ConfigException {
        super(src, name, host);
    }

    @Override
    protected ServerConfig makeItem(JSONObject src) throws ConfigException {
        return new ServerConfig(src, this);
    }

}
