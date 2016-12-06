package ru.otr.nzx.config.postprocessing;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import ru.otr.nzx.config.Config;

public class ActionConfig extends Config {
    public final static String ENABLE = "enable";
    public final static String ACTION_CLASS = "action_class";
    public final static String PARAMETERS = "parameters";

    public boolean enable;
    public final String action_class;

    public final Map<String, String> parameters = new HashMap<>();
    public boolean parametersUpdatedMark = true;

    public ActionConfig(JSONObject src, ActionConfigMap actions) throws URISyntaxException {
        super(src.getString(NAME), actions);
        enable = src.optBoolean(ENABLE, true);
        action_class = src.getString(ACTION_CLASS);
        for (Object key : src.keySet()) {
            if (ENABLE.equals(key)) {
                continue;
            }
            if (ACTION_CLASS.equals(key)) {
                continue;
            }
            parameters.put((String) key, src.getString((String) key));
        }
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put(NAME, name);
        json.put(ENABLE, enable);
        json.put(ACTION_CLASS, action_class);
        for (Map.Entry<String, String> item : parameters.entrySet()) {
            if (item.getValue() == null || item.getValue().length() == 0) {
                json.remove(item.getKey());
            } else {
                json.put(item.getKey(), item.getValue());
            }
        }
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
