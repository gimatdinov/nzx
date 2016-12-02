package ru.otr.nzx.config.http;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.otr.nzx.config.Config;
import ru.otr.nzx.config.http.location.LocationConfig;
import ru.otr.nzx.config.postprocessing.PostProcessorConfig;

public class HTTPServerConfig extends Config {
	private final static Logger log = LoggerFactory.getLogger(HTTPServerConfig.class);

	public final static String ENABLE = "enable";
	public final static String NAME = "name";
	public final static String LISTEN = "listen";

	public final static String CONNECT_TIMEOUT = "connect_timeout";
	public final static String IDLE_CONNECTION_TIMEOUT = "idle_connection_timeout";
	public final static String MAX_REQUEST_BUFFER_SIZE = "max_request_buffer_size";
	public final static String MAX_RESPONSE_BUFFER_SIZE = "max_response_buffer_size";

	public final static String LOCATIONS = "locations";
	public final static String POST_PROCESSING = "post_processing";

	public final boolean enable;
	public final String name;
	public final String listenHost;
	public final int listenPort;

	public final int connect_timeout;
	public final int idle_connection_timeout;

	public final int max_request_buffer_size;
	public final int max_response_buffer_size;

	public final Map<String, LocationConfig> locations;
	public final PostProcessorConfig post_processing;

	public String getListen() {
		return listenHost + ":" + listenPort;
	}

	public HTTPServerConfig(JSONObject src, String route, final Map<String, Object> routes) throws URISyntaxException {
		super(route + "/" + src.getString(NAME), routes);
		enable = src.optBoolean(ENABLE, true);
		name = src.getString(NAME);
		String[] listen = src.getString(LISTEN).split(":");
		listenHost = listen[0];
		listenPort = Integer.valueOf(listen[1]);

		connect_timeout = src.optInt(CONNECT_TIMEOUT);
		idle_connection_timeout = src.optInt(IDLE_CONNECTION_TIMEOUT);

		max_request_buffer_size = src.optInt(MAX_REQUEST_BUFFER_SIZE);
		max_response_buffer_size = src.optInt(MAX_RESPONSE_BUFFER_SIZE);

		locations = new HashMap<String, LocationConfig>();
		routes.put(route + "/" + name + "/" + LOCATIONS, locations);
		JSONArray locationArray = src.getJSONArray(LOCATIONS);
		for (int i = 0; i < locationArray.length(); i++) {
			JSONObject loc = locationArray.getJSONObject(i);
			String path = LocationConfig.cleanPath(loc.getString(LocationConfig.PATH));
			locations.put(path, new LocationConfig(i, path, loc, route + "/" + name + "/" + LOCATIONS, routes));
		}
		if (src.has(POST_PROCESSING)) {
			post_processing = new PostProcessorConfig(src.getJSONObject(POST_PROCESSING), route + "/" + name + "/" + POST_PROCESSING, routes);
		} else {
			post_processing = null;
		}
	}

	@Override
	public JSONObject toJSON() {
		JSONObject server = new JSONObject();
		if (!enable) {
			server.put(ENABLE, enable);
		}
		server.put(NAME, name);
		server.put(LISTEN, getListen());

		if (connect_timeout > 0) {
			server.put(CONNECT_TIMEOUT, connect_timeout);
		}
		if (idle_connection_timeout > 0) {
			server.put(IDLE_CONNECTION_TIMEOUT, idle_connection_timeout);
		}
		if (max_request_buffer_size > 0) {
			server.put(MAX_REQUEST_BUFFER_SIZE, max_request_buffer_size);
		}
		if (max_response_buffer_size > 0) {
			server.put(MAX_RESPONSE_BUFFER_SIZE, max_response_buffer_size);
		}
		for (Map.Entry<String, LocationConfig> entry : locations.entrySet()) {
			server.append(LOCATIONS, entry.getValue().toJSON().put(LocationConfig.PATH, entry.getKey()));
		}
		if (post_processing != null) {
			server.put(POST_PROCESSING, post_processing.toJSON());
		}
		return server;
	}

	public LocationConfig locate(String path) {
		path = (path != null) ? path : "/";
		String part[] = LocationConfig.cleanPath(path).split("/");
		if (part.length == 0) {
			return locations.get("/");
		}
		LocationConfig loc = null;
		for (int i = 0; i < part.length; i++) {
			StringBuilder used = new StringBuilder();
			for (int j = 0; j < part.length - i; j++) {
				used.append("/");
				used.append(part[j]);
			}
			String cursor = LocationConfig.cleanPath(used.toString());
			log.debug(path + " > " + cursor);
			loc = locations.get(cursor);
			if (loc != null) {
				log.debug(path + " = " + loc.path);
				return loc;
			}
		}
		log.debug(path + " not found");
		return null;
	}

}
