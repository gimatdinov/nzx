package ru.otr.nzx.config.http;

import java.net.URISyntaxException;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.otr.nzx.config.Config;
import ru.otr.nzx.config.ConfigMap;

public class HTTPServerConfigMap extends ConfigMap<HTTPServerConfig> {

    public HTTPServerConfigMap(JSONArray src, String name, Config host) throws URISyntaxException  {
        super(src, name, host);
    }

    @Override
    protected HTTPServerConfig makeItem(JSONObject src) throws URISyntaxException {
        return new HTTPServerConfig(src, this);
    }

}
