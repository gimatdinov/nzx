package ru.otr.nzx.config;

import java.net.URISyntaxException;

import org.json.JSONObject;

public class NZXConfig {
	public final FTPConfig ftp;
	public final HTTPConfig http;

	public NZXConfig(String src) throws URISyntaxException {
		JSONObject config = new JSONObject(src);
		if (config.has("ftp")) {
			ftp = new FTPConfig(config.getJSONObject("ftp"));
		} else {
			ftp = null;
		}
		http = new HTTPConfig(config.getJSONObject("http"));

	}

	public JSONObject toJSON() {
		JSONObject config = new JSONObject();
		if (ftp != null) {
			config.put("ftp", ftp.toJSON());
		}
		config.put("http", http.toJSON());
		return config;
	}

	@Override
	public String toString() {
		return toJSON().toString();
	}

}
