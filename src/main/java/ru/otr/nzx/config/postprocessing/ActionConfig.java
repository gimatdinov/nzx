package ru.otr.nzx.config.postprocessing;

import java.net.URISyntaxException;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.otr.nzx.config.Config;

public class ActionConfig extends Config {
    public final static String ENABLE = "enable";
    public final static String CLASS = "class";
    public final static String PARAMETERS = "parameters";

    public final boolean enable;
    public final String clazz;
    public final String[] parameters;

    public ActionConfig(JSONObject src, String route, Map<String, Config> routes) throws URISyntaxException {
        super(src, route, routes);
        enable = src.optBoolean(ENABLE, true);
        clazz = src.getString(CLASS);
        JSONArray paramArray = src.getJSONArray(PARAMETERS);
        parameters = new String[paramArray.length()];
        for (int i = 0; i < paramArray.length(); i++) {
            parameters[i] = paramArray.getString(i);
        }
    }

    @Override
    public JSONObject toJSON() {
        JSONObject action = new JSONObject();
        if (!enable) {
            action.put(ENABLE, enable);
        }
        action.put(CLASS, clazz);
        for (String item : parameters) {
            action.append(PARAMETERS, item);
        }
        return action;
    }

}
