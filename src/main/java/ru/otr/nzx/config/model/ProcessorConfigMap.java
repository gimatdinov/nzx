package ru.otr.nzx.config.model;

import java.net.URISyntaxException;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProcessorConfigMap extends ConfigMap<ProcessorConfig> {

    public ProcessorConfigMap(JSONArray src, String name, Config host) throws URISyntaxException {
        super(src, name, host);
    }

    @Override
    protected ProcessorConfig makeItem(JSONObject src) throws URISyntaxException {
        return new ProcessorConfig(src, this);
    }

}
