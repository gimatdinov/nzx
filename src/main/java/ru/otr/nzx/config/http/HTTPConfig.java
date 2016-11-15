package ru.otr.nzx.config.http;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class HTTPConfig {
	public final static String SERVERS = "servers";

	public final List<HTTPServerConfig> servers;

	public HTTPConfig(JSONObject src) throws URISyntaxException {
		servers = new ArrayList<HTTPServerConfig>();
		JSONArray srvArray = src.getJSONArray(SERVERS);
		for (int i = 0; i < srvArray.length(); i++) {
			servers.add(new HTTPServerConfig(srvArray.getJSONObject(i)));
		}
	}

	public JSONObject toJSON() {
		JSONObject http = new JSONObject();
		for (HTTPServerConfig item : servers) {
			http.append(SERVERS, item.toJSON());
		}
		return http;
	}

	@Override
	public String toString() {
		return toJSON().toString();
	}

}
