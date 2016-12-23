package ru.otr.nzx.config.model;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.otr.nzx.config.service.ConfigException;

public class PostProcessorConfigMap extends ConfigMap<PostProcessorConfig> {

    public PostProcessorConfigMap(JSONArray src, String name, Config host) throws ConfigException {
        super(src, name, host);
    }

    @Override
    protected PostProcessorConfig makeItem(JSONObject src) throws ConfigException {
        return new PostProcessorConfig(src, this);
    }

}
