package ru.otr.nzx.config.model;

import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

public class ActionConfig extends Config {
    public final static String ENABLE = "enable";
    public final static String ACTION_CLASS = "action_class";
    public final static String PROCESSOR_NAME = "processor_name";
    public final static String PARAMETERS = "parameters";

    public boolean enable;
    public final String action_class;
    public final String processor_name;
    public final SimpleConfig parameters;

    public ActionConfig(JSONObject src, Config host) throws URISyntaxException {
        super(src.getString(NAME), host);
        enable = src.optBoolean(ENABLE, true);
        action_class = src.optString(ACTION_CLASS, null);
        processor_name = src.optString(PROCESSOR_NAME, null);
        if (action_class == null && processor_name == null) {
            throw new JSONException(ACTION_CLASS + " or " + PROCESSOR_NAME + " required!");
        }
        if (action_class != null && processor_name != null) {
            throw new JSONException("Incompatible {" + ACTION_CLASS + ", " + PROCESSOR_NAME + "}");
        }
        parameters = new SimpleConfig(src.optJSONObject(PARAMETERS), PARAMETERS, this);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put(NAME, name);
        json.put(ENABLE, enable);
        json.put(ACTION_CLASS, action_class);
        json.put(PROCESSOR_NAME, processor_name);
        json.put(PARAMETERS, parameters.toJSON());
        return json;
    }

}
