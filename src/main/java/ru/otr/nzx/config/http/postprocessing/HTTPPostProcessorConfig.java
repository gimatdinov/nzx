package ru.otr.nzx.config.http.postprocessing;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class HTTPPostProcessorConfig {
    public final static String ENABLE = "enable";
    public final static String ACTIONS = "actions";

    public final boolean enable;
    public final List<ActionConfig> actions;

    public HTTPPostProcessorConfig(JSONObject src) {
        enable = src.optBoolean(ENABLE, true);
        actions = new ArrayList<>();
        if (src.has(ACTIONS)) {
            JSONArray actArray = src.getJSONArray(ACTIONS);
            for (int i = 0; i < actArray.length(); i++) {
                actions.add(new ActionConfig(actArray.getJSONObject(i)));
            }
        }

    }

    public JSONObject toJSON() {
        JSONObject server = new JSONObject();
        if (!enable) {
            server.put(ENABLE, enable);
        }
        for (ActionConfig item : actions) {
            server.append(ACTIONS, item.toJSON());
        }
        return server;
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }
}
