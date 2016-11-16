package ru.otr.nzx.config.postprocessing;

import org.json.JSONArray;
import org.json.JSONObject;

public class ActionConfig {
    public final static String ENABLE = "enable";
    public final static String CLASS = "class";
    public final static String PARAMETERS = "parameters";

    public final boolean enable;
    public final String clazz;
    public final String[] parameters;

    public ActionConfig(JSONObject src) {
        enable = src.optBoolean(ENABLE, true);
        clazz = src.getString(CLASS);
        JSONArray paramArray = src.getJSONArray(PARAMETERS);
        parameters = new String[paramArray.length()];
        for (int i = 0; i < paramArray.length(); i++) {
            parameters[i] = paramArray.getString(i);
        }
    }

    public Object toJSON() {
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

    @Override
    public String toString() {
        return toJSON().toString();
    }

}
