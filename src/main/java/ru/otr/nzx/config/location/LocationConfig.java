package ru.otr.nzx.config.location;

import org.json.JSONObject;

public abstract class LocationConfig {
	public final String path;

	public LocationConfig(String path) {
		this.path = path;
	}

	public JSONObject toJSON() {
		return new JSONObject().put("path", path);
	}

	@Override
	public String toString() {
		return toJSON().toString();
	}

}
