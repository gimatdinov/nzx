package ru.otr.nzx.config.model;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.otr.nzx.config.service.ConfigException;

public class ProcessorConfigMap extends ConfigMap<ProcessorConfig> {

    public ProcessorConfigMap(JSONArray src, String name, Config host) throws ConfigException {
        super(src, name, host);
    }

    @Override
    protected ProcessorConfig makeItem(JSONObject src) throws ConfigException {
        return new ProcessorConfig(src, this);
    }

}
