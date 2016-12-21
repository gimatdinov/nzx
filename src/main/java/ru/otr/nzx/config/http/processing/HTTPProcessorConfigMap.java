package ru.otr.nzx.config.http.processing;

import java.net.URISyntaxException;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.otr.nzx.config.ConfigMap;
import ru.otr.nzx.config.http.HTTPConfig;

public class HTTPProcessorConfigMap extends ConfigMap<HTTPProcessorConfig> {

    public HTTPProcessorConfigMap(JSONArray src, String name, HTTPConfig http) throws URISyntaxException {
        super(src, name, http);
    }

    @Override
    protected HTTPProcessorConfig makeItem(JSONObject src) throws URISyntaxException {
        return new HTTPProcessorConfig(src, this);
    }

}
