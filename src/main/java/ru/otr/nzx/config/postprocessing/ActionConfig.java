package ru.otr.nzx.config.postprocessing;

import java.net.URISyntaxException;
import java.util.Map;

import org.json.JSONObject;

import ru.otr.nzx.config.Config;
import ru.otr.nzx.config.SimpleConfig;

public class ActionConfig extends Config {
    public final static String ENABLE = "enable";
    public final static String ACTION_CLASS = "action_class";
    public final static String PARAMETERS = "parameters";

    public boolean enable;
    public final String action_class;

    public final SimpleConfig parameters;
    public boolean parametersUpdatedMark = true;

    public ActionConfig(JSONObject src, ActionConfigMap actions) throws URISyntaxException {
        super(src.getString(NAME), actions);
        enable = src.optBoolean(ENABLE, true);
        action_class = src.getString(ACTION_CLASS);
        parameters = new SimpleConfig(src.optJSONObject(PARAMETERS), PARAMETERS, this);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put(NAME, name);
        json.put(ENABLE, enable);
        json.put(ACTION_CLASS, action_class);
        json.put(PARAMETERS, parameters.toJSON());
        return json;
    }

    public void setParameters(Map<String, String> parameters) {
        for (Map.Entry<String, String> item : parameters.entrySet()) {
            if (item.getKey() == null) {
                continue;
            }
            if (NAME.equals(item.getKey())) {
                continue;
            }
            if (ENABLE.equals(item.getKey())) {
                enable = Boolean.valueOf(item.getValue());
                continue;
            }
            if (ACTION_CLASS.equals(item.getKey())) {
                continue;
            }
            this.parameters.put(item.getKey(), item.getValue());
        }
        parametersUpdatedMark = true;
    }

}
