package ru.otr.nzx.config.postprocessing;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class PostProcessorConfig {
	public final static String ENABLE = "enable";
	public final static String MAX_COUNT_OF_TANKS = "max_count_of_tanks";
	public final static String WORKERS = "workers";
	public final static String ACTIONS = "actions";

	public final boolean enable;
	public final int max_count_of_tanks;
	public final int workers;
	public final List<ActionConfig> actions;

	public PostProcessorConfig(JSONObject src) {
		enable = src.optBoolean(ENABLE, true);
		max_count_of_tanks = src.optInt(MAX_COUNT_OF_TANKS, 0);
		workers = src.optInt(WORKERS, 1);
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
		if (max_count_of_tanks > 0) {
			server.put(MAX_COUNT_OF_TANKS, max_count_of_tanks);
		}
		if (workers > 1) {
			server.put(WORKERS, workers);
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
