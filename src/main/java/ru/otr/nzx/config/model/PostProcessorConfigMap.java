package ru.otr.nzx.config.model;

import java.net.URISyntaxException;

import org.json.JSONArray;
import org.json.JSONObject;

public class PostProcessorConfigMap extends ConfigMap<PostProcessorConfig> {

    public PostProcessorConfigMap(JSONArray src, String name, Config host) throws URISyntaxException {
        super(src, name, host);
    }

    @Override
    protected PostProcessorConfig makeItem(JSONObject src) throws URISyntaxException {
        return new PostProcessorConfig(src, this);
    }

}
