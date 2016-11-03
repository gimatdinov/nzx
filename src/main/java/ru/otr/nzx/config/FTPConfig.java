package ru.otr.nzx.config;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class FTPConfig {
	public final List<FTPServerConfig> servers;

	public FTPConfig(JSONObject src) throws URISyntaxException {
		servers = new ArrayList<>();
		JSONArray srvArray = src.getJSONArray("servers");
		for (Object item : srvArray) {
			servers.add(new FTPServerConfig((JSONObject) item));
		}
	}

	public JSONObject toJSON() {
		JSONObject http = new JSONObject();
		for (FTPServerConfig item : servers) {
			http.append("servers", item.toJSON());
		}
		return http;
	}

	@Override
	public String toString() {
		return toJSON().toString();
	}
}
