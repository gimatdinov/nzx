package ru.otr.nzx.config.postprocessing;

import java.net.URISyntaxException;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.otr.nzx.config.Config;
import ru.otr.nzx.config.ConfigMap;

public class ActionConfigMap extends ConfigMap<ActionConfig> {

    public ActionConfigMap(JSONArray src, String name, Config host) throws URISyntaxException {
        super(src, name, host);
    }

    @Override
    protected ActionConfig makeItem(JSONObject src, String name) throws URISyntaxException {
        return new ActionConfig(src, name, this);
    }

}
