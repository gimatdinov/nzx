package ru.otr.nzx.config.model;

import org.json.JSONArray;
import org.json.JSONObject;

public class ActionConfigMap extends ConfigMap<ActionConfig> {

	public ActionConfigMap(JSONArray src, String name, Config host) throws ConfigException {
		super(src, name, host);
	}

	@Override
	protected ActionConfig makeItem(JSONObject src) throws ConfigException {
		return new ActionConfig(src, this);
	}

}
