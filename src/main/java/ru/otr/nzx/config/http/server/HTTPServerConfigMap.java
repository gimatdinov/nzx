package ru.otr.nzx.config.http.server;

import java.net.URISyntaxException;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.otr.nzx.config.ConfigMap;
import ru.otr.nzx.config.http.HTTPConfig;

public class HTTPServerConfigMap extends ConfigMap<HTTPServerConfig> {

    public HTTPServerConfigMap(JSONArray src, String name, HTTPConfig http) throws URISyntaxException  {
        super(src, name, http);
    }

    @Override
    protected HTTPServerConfig makeItem(JSONObject src) throws URISyntaxException {
        return new HTTPServerConfig(src, this);
    }

}
