package ru.otr.nzx.config;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class HTTPConfig {
	public final List<HTTPServerConfig> servers;

	public HTTPConfig(JSONObject src) throws URISyntaxException {
		servers = new ArrayList<HTTPServerConfig>();
		JSONArray srvArray = src.getJSONArray("servers");
		for (Object item : srvArray) {
			servers.add(new HTTPServerConfig((JSONObject) item));
		}
	}

	public JSONObject toJSON() {
		JSONObject http = new JSONObject();
		for (HTTPServerConfig item : servers) {
			http.append("servers", item.toJSON());
		}
		return http;
	}
	
	@Override
	public String toString() {
		return toJSON().toString();
	}

}
