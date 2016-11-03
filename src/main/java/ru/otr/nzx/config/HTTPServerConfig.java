package ru.otr.nzx.config;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.otr.nzx.config.location.LocationConfig;
import ru.otr.nzx.config.location.ProxyPassLocationConfig;

public class HTTPServerConfig {
	public final String listenHost;
	public final int listenPort;

	public final int max_request_buffer_size;
	public final int max_response_buffer_size;

	public final Map<String, LocationConfig> locations;

	public HTTPServerConfig(JSONObject src) throws URISyntaxException {
		String[] listen = src.getString("listen").split(":");
		listenHost = listen[0];
		listenPort = Integer.valueOf(listen[1]);

		max_request_buffer_size = src.optInt("max_request_buffer_size");
		max_response_buffer_size = src.optInt("max_response_buffer_size");

		locations = new HashMap<String, LocationConfig>();
		JSONArray locationArray = src.getJSONArray("locations");
		for (Object item : locationArray) {
			JSONObject loc = (JSONObject) item;
			String path = NZXConfigHelper.cleanPath(loc.getString("path"));
			if (loc.has("proxy_pass")) {
				locations.put(path, new ProxyPassLocationConfig(path, loc));
			}
		}
	}

	public JSONObject toJSON() {
		JSONObject server = new JSONObject();
		server.put("listen", listenHost + ":" + listenPort);
		server.put("max_request_buffer_size", max_request_buffer_size);
		server.put("max_response_buffer_size", max_response_buffer_size);
		for (Map.Entry<String, LocationConfig> entry : locations.entrySet()) {
			server.append("locations", entry.getValue().toJSON().put("path", entry.getKey()));
		}
		return server;
	}
	
	@Override
	public String toString() {
		return toJSON().toString();
	}

}
