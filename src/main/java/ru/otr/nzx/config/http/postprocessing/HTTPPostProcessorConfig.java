package ru.otr.nzx.config.http.postprocessing;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class HTTPPostProcessorConfig {
    public final static String ENABLE = "enable";
    public final static String TANK_CAPACITY = "tank_capacity";
    public final static String DUMPING_ENABEL = "dumping_enable";
    public final static String ACTIONS = "actions";

    public final boolean enable;
    public final int tank_capacity;
    public final boolean dumping_enable;
    public final List<ActionConfig> actions;

    public HTTPPostProcessorConfig(JSONObject src) {
        enable = src.optBoolean(ENABLE, true);
        tank_capacity = src.getInt(TANK_CAPACITY);
        dumping_enable = src.optBoolean(DUMPING_ENABEL);
        actions = new ArrayList<>();
        if (src.has(ACTIONS)) {
            JSONArray actArray = src.getJSONArray(ACTIONS);
            for (int i = 0; i < actArray.length(); i++) {
                actions.add(new ActionConfig(actArray.getJSONObject(i)));
            }
        }

    }

    public JSONObject toJSON() {
        JSONObject postProcessor = new JSONObject();
        postProcessor.put(ENABLE, enable);
        postProcessor.put(TANK_CAPACITY, tank_capacity);
        postProcessor.put(DUMPING_ENABEL, dumping_enable);
        for (ActionConfig item : actions) {
            postProcessor.append(ACTIONS, item.toJSON());
        }
        return postProcessor;
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }
}
