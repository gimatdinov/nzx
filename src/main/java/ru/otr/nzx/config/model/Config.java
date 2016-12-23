package ru.otr.nzx.config.model;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class Config {
	public final static String NAME_FORMAT = "[a-zA-Z_][a-zA-Z0-9_.-]*";

	public final static String ENABLE = "enable";
	public final static String NAME = "name";
	public final static String REF = "ref:";

	protected final String name;
	protected final Config host;
	protected final Map<String, Config> context;

	public Config(String name, Config host) throws ConfigException {
		if (host != null) {
			if (name == null || !name.matches(NAME_FORMAT)) {
				throw new ConfigException("Invalid name: \"" + name + "\"");
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

	public void bindPathName() throws ConfigException {
		if (context.containsKey(getPathName())) {
			throw new ConfigException("PathName " + getPathName() + " already bound!");
		}
		context.put(getPathName(), this);
		if (host != null) {
			if (context.containsKey(getPathName() + "/")) {
				throw new ConfigException("PathName " + getPathName() + "/ already bound!");
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

	public void bindRefName() throws ConfigException {
		if (context.containsKey(REF + getName())) {
			throw new ConfigException("Referece" + REF + getName() + " already bound!");
		}
		context.put(REF + getName(), this);
	}

	public void unbindRefName() {
		context.remove(REF + getName());
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
