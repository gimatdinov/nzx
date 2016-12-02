package ru.otr.nzx.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.json.JSONObject;

public abstract class Config {

	protected final String route;
	protected final Map<String, Object> routes;

	public Config(String route, Map<String, Object> routes) throws URISyntaxException {
		new URI(route);
		this.route = route;
		this.routes = routes;
		if (routes.containsKey(route)) {
			throw new URISyntaxException(route, "Route already bound!");
		}
		routes.put(route, this);
	}

	public void unregisterRoute() {
		routes.remove(route);
	}

	@Override
	public String toString() {
		return toJSON().toString();
	}

	public String toString(int indentFactor) {
		return toJSON().toString(indentFactor);
	}

	public abstract JSONObject toJSON();

}
