package ru.otr.nzx.config;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class FTPServerConfig {
	public final String listenHost;
	public final int listenPort;
	public final String directory;
	public final boolean anonymous_enable;

	public final List<FTPUserConfig> users;

	public FTPServerConfig(JSONObject src) {
		String[] listen = src.getString("listen").split(":");
		listenHost = listen[0];
		listenPort = Integer.valueOf(listen[1]);
		directory = src.getString("directory");
		anonymous_enable = src.optBoolean("anonymous_enable");

		users = new ArrayList<>();
		if (!anonymous_enable) {
			JSONArray userArray = src.getJSONArray("users");
			for (Object item : userArray) {
				users.add(new FTPUserConfig((JSONObject) item));
			}
		}
	}

	public JSONObject toJSON() {
		JSONObject server = new JSONObject();
		server.put("listen", listenHost + ":" + listenPort);
		server.put("directory", directory);
		server.put("anonymous_enable", anonymous_enable);
		for (FTPUserConfig item : users) {
			server.append("users", item.toJSON());
		}
		return server;
	}

	@Override
	public String toString() {
		return toJSON().toString();
	}

}
