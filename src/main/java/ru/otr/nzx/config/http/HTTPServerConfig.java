package ru.otr.nzx.config.http;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.otr.nzx.config.NZXConfigHelper;
import ru.otr.nzx.config.http.location.LocationConfig;
import ru.otr.nzx.config.http.location.ProxyPassLocationConfig;
import ru.otr.nzx.config.postprocessing.PostProcessorConfig;

public class HTTPServerConfig {
	public final static String ENABLE = "enable";
	public final static String NAME = "name";
	public final static String LISTEN = "listen";
	public final static String MAX_REQUEST_BUFFER_SIZE = "max_request_buffer_size";
	public final static String MAX_RESPONSE_BUFFER_SIZE = "max_response_buffer_size";
	public final static String LOCATIONS = "locations";
	public final static String POST_PROCESSING = "post_processing";

	public final boolean enable;
	public final String name;
	public final String listenHost;
	public final int listenPort;

	public final int max_request_buffer_size;
	public final int max_response_buffer_size;

	public final Map<String, LocationConfig> locations;
	public final PostProcessorConfig post_processing;

	public String getListen() {
		return listenHost + ":" + listenPort;
	}

	public HTTPServerConfig(JSONObject src) throws URISyntaxException {
		enable = src.optBoolean(ENABLE, true);
		name = src.getString(NAME);
		String[] listen = src.getString(LISTEN).split(":");
		listenHost = listen[0];
		listenPort = Integer.valueOf(listen[1]);

		max_request_buffer_size = src.optInt(MAX_REQUEST_BUFFER_SIZE);
		max_response_buffer_size = src.optInt(MAX_RESPONSE_BUFFER_SIZE);

		locations = new HashMap<String, LocationConfig>();
		JSONArray locationArray = src.getJSONArray(LOCATIONS);
		for (int i = 0; i < locationArray.length(); i++) {
			JSONObject loc = locationArray.getJSONObject(i);
			String path = NZXConfigHelper.cleanPath(loc.getString(LocationConfig.PATH));
			if (loc.has(ProxyPassLocationConfig.PROXY_PASS)) {
				locations.put(path, new ProxyPassLocationConfig(path, loc));
			}
		}
		if (src.has(POST_PROCESSING)) {
			post_processing = new PostProcessorConfig(src.getJSONObject(POST_PROCESSING));
		} else {
			post_processing = null;
		}

	}

	public JSONObject toJSON() {
		JSONObject server = new JSONObject();
		if (!enable) {
			server.put(ENABLE, enable);
		}
		server.put(NAME, name);
		server.put(LISTEN, getListen());
		server.put(MAX_REQUEST_BUFFER_SIZE, max_request_buffer_size);
		server.put(MAX_RESPONSE_BUFFER_SIZE, max_response_buffer_size);
		for (Map.Entry<String, LocationConfig> entry : locations.entrySet()) {
			server.append(LOCATIONS, entry.getValue().toJSON().put(LocationConfig.PATH, entry.getKey()));
		}
		if (post_processing != null) {
			server.put(POST_PROCESSING, post_processing.toJSON());
		}
		return server;
	}

	@Override
	public String toString() {
		return toJSON().toString();
	}

}
