package ru.otr.nzx.config.postprocessing;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import ru.otr.nzx.config.Config;
import ru.otr.nzx.postprocessing.NZXAction;

public class ActionConfig extends Config {
	public final static String INDEX = "index";
	public final static String ENABLE = "enable";
	public final static String ACTION_CLASS = "action_class";
	public final static String PARAMETERS = "parameters";

	public final int index;
	public boolean enable;
	public final String action_class;

	private final Map<String, String> parameters = new HashMap<>();
	private NZXAction action;

	public ActionConfig(int index, JSONObject src, String route, Map<String, Object> routes) throws URISyntaxException {
		super(route + "/" + index, routes);
		this.index = index;
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
		json.put(INDEX, index);
		json.put(ENABLE, enable);
		json.put(ACTION_CLASS, action_class);
		for (Map.Entry<String, String> item : parameters.entrySet()) {
			json.put(item.getKey(), item.getValue());
		}
		return json;
	}

	public boolean isEnable() {
		return enable;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		for (Map.Entry<String, String> item : parameters.entrySet()) {
			if (INDEX.equals(item.getKey())) {
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
	}

	public NZXAction getAction() {
		return action;
	}

	public void setAction(NZXAction action) {
		this.action = action;
	}

}
