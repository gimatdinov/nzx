package ru.otr.nzx.config.model;

import java.net.URISyntaxException;

import org.json.JSONArray;
import org.json.JSONObject;

public class ActionConfigMap extends ConfigMap<ActionConfig> {

    public ActionConfigMap(JSONArray src, String name, Config host) throws URISyntaxException {
        super(src, name, host);
    }

    @Override
    protected ActionConfig makeItem(JSONObject src) throws URISyntaxException {
        return new ActionConfig(src, this);
    }

}
