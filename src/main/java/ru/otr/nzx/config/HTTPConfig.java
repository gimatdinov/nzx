package ru.otr.nzx.config;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class HTTPConfig {
	public final static String SERVERS = "servers";
	public final static String POST_PROCESSING = "post_processing";

	public final List<HTTPServerConfig> servers;
	public final HTTPPostProcessorConfig post_processing;

	public HTTPConfig(JSONObject src) throws URISyntaxException {
		servers = new ArrayList<HTTPServerConfig>();
		JSONArray srvArray = src.getJSONArray(SERVERS);
		for (int i = 0; i < srvArray.length(); i++) {
			servers.add(new HTTPServerConfig(srvArray.getJSONObject(i)));
		}
		if (src.has(POST_PROCESSING)) {
			post_processing = new HTTPPostProcessorConfig(src.getJSONObject(POST_PROCESSING));
		} else {
			post_processing = null;
		}
	}

	public JSONObject toJSON() {
		JSONObject http = new JSONObject();
		for (HTTPServerConfig item : servers) {
			http.append(SERVERS, item.toJSON());
		}
		if (post_processing != null) {
			http.put(POST_PROCESSING, post_processing.toJSON());
		}
		return http;
	}

	@Override
	public String toString() {
		return toJSON().toString();
	}

}
