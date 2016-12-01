package ru.otr.nzx.config.postprocessing;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.otr.nzx.config.Config;

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
    public final List<ActionConfig> actions;

    public PostProcessorConfig(JSONObject src, String route, final Map<String, Config> routes) throws URISyntaxException {
        super(src, route, routes);
        enable = src.optBoolean(ENABLE, true);
        buffer_pool_size = src.getInt(BUFFER_POOL_SIZE);
        buffer_size_min = src.getInt(BUFFER_SIZE_MIN);
        workers = src.optInt(WORKERS, 1);
        actions = new ArrayList<>();
        if (src.has(ACTIONS)) {
            JSONArray actArray = src.getJSONArray(ACTIONS);
            for (int i = 0; i < actArray.length(); i++) {
                actions.add(new ActionConfig(actArray.getJSONObject(i), route + "/" + i, routes));
            }
        }
    }

    @Override
    public JSONObject toJSON() {
        JSONObject server = new JSONObject();
        if (!enable) {
            server.put(ENABLE, enable);
        }
        server.put(BUFFER_POOL_SIZE, buffer_pool_size);
        server.put(BUFFER_SIZE_MIN, buffer_size_min);
        if (workers > 1) {
            server.put(WORKERS, workers);
        }
        for (ActionConfig item : actions) {
            server.append(ACTIONS, item.toJSON());
        }
        return server;
    }

}
