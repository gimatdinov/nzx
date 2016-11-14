package ru.otr.nzx.config;

import org.json.JSONObject;

public class HTTPPostProcessorConfig {
	public final static String ENABLE = "enable";
	public final static String TANK_CAPACITY = "tank_capacity";
	public final static String ADD_DUMPING = "add_dumping";

	public final boolean enable;
	public final int tank_capacity;
	public final boolean add_dumping;

	public HTTPPostProcessorConfig(JSONObject src) {
		enable = src.getBoolean(ENABLE);
		tank_capacity = src.getInt(TANK_CAPACITY);
		add_dumping = src.optBoolean(ADD_DUMPING);
	}

	public JSONObject toJSON() {
		JSONObject postProcessor = new JSONObject();
		postProcessor.put(ENABLE, enable);
		postProcessor.put(TANK_CAPACITY, tank_capacity);
		postProcessor.put(ADD_DUMPING, add_dumping);
		return postProcessor;
	}

	@Override
	public String toString() {
		return toJSON().toString();
	}
}
