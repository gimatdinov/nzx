package ru.otr.nzx.config.model;

import java.net.URISyntaxException;

import org.json.JSONObject;

public class PostProcessorConfig extends Config {
    public final static String ENABLE = "enable";
    public final static String BUFFER_POOL_SIZE = "buffer_pool_size";
    public final static String BUFFER_SIZE_MIN = "buffer_size_min";
    public final static String WORKERS = "workers";
    public final static String ACTIONS = "actions";

    public final boolean enable;
    public final int buffer_pool_size;
    public final int buffer_size_min;
    public final int workers;
    public final ActionConfigMap actions;

    public PostProcessorConfig(JSONObject src, Config host) throws URISyntaxException {
        super(src.getString(NAME), host);
        enable = src.optBoolean(ENABLE, true);
        buffer_pool_size = src.getInt(BUFFER_POOL_SIZE);
        buffer_size_min = src.getInt(BUFFER_SIZE_MIN);
        workers = src.optInt(WORKERS, 1);
        actions = new ActionConfigMap(src.optJSONArray(ACTIONS), ACTIONS, this);
        context.put(REF + getName(), this);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put(NAME, name);
        if (!enable) {
            json.put(ENABLE, enable);
        }
        json.put(BUFFER_POOL_SIZE, buffer_pool_size);
        json.put(BUFFER_SIZE_MIN, buffer_size_min);
        if (workers > 1) {
            json.put(WORKERS, workers);
        }
        json.put(ACTIONS, actions.toJSON());
        return json;
    }

}
