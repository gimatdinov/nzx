package ru.otr.nzx.config;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class Config {
	public final static String NAME_FORMAT = "[a-zA-Z_][a-zA-Z0-9_.-]*";

	public final static String ENABLE = "enable";
	public final static String NAME = "name";

	protected final String name;
	protected final Config host;
	protected final Map<String, Config> context;

	public Config(String name, Config host) throws URISyntaxException {
		if (host != null) {
			if (name == null || !name.matches(NAME_FORMAT)) {
				throw new URISyntaxException("\"" + name + "\"", "Invalid name!");
			}
			this.name = name;
			this.host = host;
			context = host.getContext();
		} else {
			this.name = "/";
			this.host = null;
			context = new HashMap<>();
		}
		bindPathName();
	}

	public void bindPathName() throws URISyntaxException {
		if (context.containsKey(getPathName())) {
			throw new URISyntaxException(getPathName(), "PathName already bound!");
		}
		context.put(getPathName(), this);
		if (host != null) {
			if (context.containsKey(getPathName() + "/")) {
				throw new URISyntaxException(getPathName() + "/", "PathName already bound!");
			}
			context.put(getPathName() + "/", this);
		}
	}

	public void unbindPathName() {
		context.remove(getPathName());
		if (host != null) {
			context.remove(getPathName() + "/");
		}
	}

	@Override
	public String toString() {
		return toJSON().toString();
	}

	public String getName() {
		return name;
	}

	public String getPathName() {
		if (host == null) {
			return name;
		} else {
			return host.getPathName() + (host.host != null ? "/" : "") + name;
		}
	}

	public Map<String, Config> getContext() {
		return context;
	}

	public String toString(int indentFactor) {
		Object json = toJSON();
		if (json instanceof JSONObject) {
			return ((JSONObject) json).toString(indentFactor);
		}
		if (json instanceof JSONArray) {
			return ((JSONArray) json).toString(indentFactor);
		}
		return json.toString();
	}

	public abstract Object toJSON();

}
